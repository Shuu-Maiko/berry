# 🛡️ Security Architecture & Mitigations

since berry allows users to schedule raw HTTP webhooks (outbound pings) and runs on a shared public cloud (render), we had to lock down the system against some extremely dangerous cloud-native vulnerabilities. 

here is how we secured the system without spending a single dollar.

---

## 1. SSRF (Server-Side Request Forgery) Block

### The Threat
if a user schedules a webhook with the URL `http://169.254.169.254/latest/meta-data/` or `http://localhost:5432`, your server would execute that request. 
*   the first URL could steal your AWS/Render cloud IAM metadata credentials.
*   the second URL could allow attackers to ping your internal database port or call internal actuator APIs (like `http://localhost:8080/actuator/shutdown`) to crash the server.

### The Fix (`SsrfValidator.java`)
before `WebhookService` makes any outgoing HTTP call, the URL is passed to `SsrfValidator`. 
1.  we parse the URL and extract the host.
2.  we perform DNS resolution using `InetAddress.getByName(host)` to resolve the host to its actual IP address. (this prevents DNS rebinding attacks).
3.  we check if the IP falls under any local or private ranges:
    ```java
    if (ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress() || ip.isSiteLocalAddress()) {
        throw new SecurityException("SSRF Blocked: Cannot ping internal networks.");
    }
    ```
this completely blocks pings to `localhost`, `127.0.0.1`, `10.x.x.x`, `192.168.x.x`, and metadata endpoints.

---

## 2. Cookie CSRF (Cross-Site Request Forgery) Protection

### The Threat
we authenticate users via a JWT token stored inside a browser cookie (`auth_token`). if we disable CSRF protection (which is standard for REST APIs), a malicious website could trigger a background request to `http://localhost:8080/api/jobs/create`. the browser would automatically attach the user's logged-in cookie, allowing the attacker to create or delete jobs on the user's behalf.

### The Fix (`AuthController.java`)
we implemented two layers of protection:
1.  **SameSite=Strict Cookies:** we added `.sameSite("Strict")` to our cookie configuration. this tells browsers never to send the cookie on cross-site requests (meaning if they are on a malicious site, their browser will not attach the `auth_token` when sending requests to our API).
2.  **Dynamic Secure Flag:** in local development, cookies cannot be sent over HTTPS since `localhost` runs on HTTP. in production, however, we must require HTTPS. 
    we created an environment variable `app.env=${APP_ENV:prod}`. in the login and logout endpoints, the secure flag dynamically toggles:
    ```java
    boolean isSecure = !"dev".equalsIgnoreCase(appEnv);
    cookie.secure(isSecure);
    ```
this gives us seamless local testing while ensuring strict HTTPS-only cookies in production.

---

## 3. BOLA / IDOR Protection (Broken Object Level Authorization)

### The Threat
if an authenticated user wants to delete a job, they could change the URL parameter to a different job ID (e.g. `DELETE /api/jobs/12345`). if the controller simply runs `delete(12345)` without checking who owns it, anyone could delete anyone else's jobs.

### The Fix
1.  **Strict Owner Validation:** in `JobService.java`, every action checking, updating, or deleting a job explicitly validates ownership:
    ```java
    if (!job.getUser().getId().equals(user.getId())) {
        throw new SecurityException("You do not own this job");
    }
    ```
    *note: we use `.equals()` instead of `!=` because java caches Long objects, and `!=` would break comparisons for user IDs above 127.*
2.  **Environment-Restricted APIs:** we had a `UserController.java` that was exposing a list of all user emails to any authenticated user (a massive PII leak). instead of deleting it (since it's useful for dev debugging), we wrapped it in a property condition:
    ```java
    @RestController
    @RequestMapping("/users")
    @ConditionalOnProperty(name = "app.env", havingValue = "dev")
    public class UserController {
    ```
    if `app.env` is `prod` (default), Spring Boot will refuse to load the controller, making it completely unreachable in production.

---

## 4. Quota Limits (DoS Prevention)

to ensure our free-tier database isn't flooded with millions of rows from a single malicious user loop-calling our job creation API, we added a hard limit inside `JobService.java`:
```java
if (jobRepository.countByUser(user) >= 10) {
    throw new RuntimeException("Free tier limit reached: You can only have 10 active jobs.");
}
```
this protects our PostgreSQL row storage limit (neon has a 1GB free limit) from being exhausted.
