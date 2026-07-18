# ⚙️ Capacity Planning & Hardware Bottlenecks

building a distributed scheduler on a zero-dollar budget is all about managing tight limits. since we are running everything on **render's free tier**, we have to work with some harsh hardware bottlenecks:

*   **RAM:** 512 MB (hard limit, the container will instantly OOM and crash if we exceed this).
*   **CPU:** 0.1 CPU share (shared dynamically, meaning CPU-heavy tasks will throttle us).
*   **Postgres Connections:** neon/supabase/render free tiers usually limit us to 10–50 concurrent connections.

here is the engineering math behind how we tuned the system to stay alive under real traffic without crashing our free hosting.

---

## 📊 The Math: Throughput & Scaling

### 1. Webhook Execution Capacity
an average HTTP webhook request (outgoing ping) takes about **500ms** to complete (network roundtrip + external server processing).

we capped our scheduler to use exactly **10 worker threads** (`jobrunr.background-job-server.worker-count=10`). 

$$\text{Throughput per worker} = \frac{1000\text{ ms}}{500\text{ ms/job}} = 2\text{ jobs/sec}$$

$$\text{Max System Throughput} = 10\text{ workers} \times 2\text{ jobs/sec} = 20\text{ jobs/sec}$$

*   **per minute:** $20 \times 60 = 1,200\text{ jobs/minute}$.
*   **per day:** $1,200 \times 1440 = 1,728,000\text{ jobs/day}$.

so on paper, our little free-tier container can trigger **1.7 million webhooks a day**!

### 2. The Slow Target Bottleneck (Tarpits)
what if a user configures a job to ping a slow server that takes **4 seconds** to respond? 
if we didn't set limits, 10 slow jobs would block all 10 worker threads, dropping our throughput to:

$$\text{Blocked Throughput} = \frac{10\text{ workers}}{4\text{ sec}} = 2.5\text{ jobs/sec} \quad (150\text{ jobs/minute})$$

to prevent this "tarpit attack," we configured our `RestClient` with a strict **5-second timeout**. no matter how slow the target is, a worker thread will free up in at most 5 seconds.

---

## 🧠 Memory Tuning (Avoiding OOM)

spring boot and hibernate are memory hogs. a default spring boot app can easily consume 350MB of RAM just sitting idle. if we add background workers, we risk crossing the **512MB Render limit**.

we did two major optimizations to stay under the limit:

### 1. Disabling Default Virtual Thread Spawning in JobRunr
by default, JobRunr checks your CPU cores and spawns threads dynamically using a `cores * 16` formula. in a containerized environment (like render), java might detect the host machine's cores (e.g. 16 or 32 cores) instead of the container's allocated CPU share. this would spawn **256+ threads**, causing instant memory bloat and OOM crashes.

we disabled this in `application.properties`:
```properties
jobrunr.background-job-server.thread-type=PlatformThreads
jobrunr.background-job-server.worker-count=10
```
this locks the thread count strictly to 10, keeping our memory usage highly predictable.

### 2. HikariCP Connection Pool Sizing
every database connection takes up memory in both the Spring app and the Postgres server. if we set our connection pool too high, we'll hit database connection limits or run out of memory. 

we tuned the pool size to **30**:
```properties
spring.datasource.hikari.maximum-pool-size=30
```
this leaves 20 connections free (if we have a 50-conn limit) for local developer connections or running a secondary web server instance.

---

## 🚧 Hard Bottlenecks (Where the System Will Break)

if you scale this system to thousands of users, here is exactly what will break first:

1.  **Database Write IOPS:** 
    every job run writes a execution state log to the database. at 1,200 jobs/minute, we are writing to disk 20 times per second. free database tiers (like neon or render postgres) will throttle disk writes once you exceed their IOPS limits.
2.  **RAM Garbage Collection Spikes:**
    if we process 20 jobs/sec, java will generate millions of short-lived objects (HTTP requests, DTOs, JSON payloads). the JVM Garbage Collector will have to run constantly. on a 0.1 CPU share, garbage collection will cause massive latency spikes, eventually leading to memory leaks or OOMs.
3.  **JobRunr Poll Interval:**
    JobRunr polls the database every 15 seconds by default to see what jobs are due. if we have 500 jobs scheduled to run at the exact same second, there will be a slight delay (latency) as the workers query and dequeue them in batches.
