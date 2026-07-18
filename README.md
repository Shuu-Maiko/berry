<div align="center">

<h1>
  Berry (<code>berry</code>)
</h1>

**Berry** is a lightweight, distributed cron job and webhook scheduler. It runs as a springboot backend that manages recurring HTTP pings, writes database logs, and triggers discord alerts when jobs fail or succeed—built to handle real traffic on a zero-dollar budget.

[Installing the service](#installing-the-service) ·
[Building from source](#building-from-source) ·
[Documentation](#documentation) ·
[Repository layout](#repository-layout) ·
[Development](#development) ·
[License](#license)

</div>

---

## Installing the service

to run berry on render's free tier:

1.  **Create a web service** on Render pointing to your fork of this repository.
2.  **Add a PostgreSQL database** (either Render's free tier, neon, or supabase).
3.  **Add a RabbitMQ broker instance** (e.g. from cloudamqp free tier).
4.  **Configure environment variables** in the Render dashboard:
    *   `DB_USERNAME`: database user
    *   `DB_PASSWORD`: database password
    *   `DATABASE_URL`: jdbc connection string
    *   `JWT_SECRET`: a 256-bit signing key (at least 32 characters)
    *   `RABBITMQ_HOST`: your rabbitmq broker hostname
    *   `RABBITMQ_PORT`: rabbitmq port (usually 5672)
    *   `RABBITMQ_USERNAME`: rabbitmq user
    *   `RABBITMQ_PASSWORD`: rabbitmq password
    *   `FRONTEND_URL`: the full https url of your deployed frontend (e.g. `https://my-berry-ui.vercel.app`)
    *   `APP_ENV`: set to `prod`

---

## Building from source

### Requirements

*   **Java 21** or higher.
*   **PostgreSQL** running locally on port 5432.
*   **RabbitMQ** running locally on port 5672.

### Local Setup

1.  Clone this repository.
2.  Create a `.env` file in the project root:
    ```env
    DB_USERNAME=postgres
    DB_PASSWORD=your_local_password
    JWT_SECRET=super_secret_local_jwt_signing_key_32_chars
    APP_ENV=dev
    ```
3.  Run the springboot backend:
    ```sh
    ./mvnw spring-boot:run
    ```
    the service will boot up and listen on port `8080`.

---

## Documentation

detailed documentation on scaling and security design is located in the `docs/` folder:

*   **[Capacity & Bottlenecks (The Math)](docs/architecture.md)** — calculations of throughput limits, JVM memory settings, and database pool constraints on 512MB RAM.
*   **[Security Architecture](docs/security.md)** — how we mitigated cloud-specific attacks like SSRF, CSRF, BOLA, and DoS.
*   **[API Reference](docs/api.md)** — full list of REST endpoints and payload structures.

---

## Repository layout

```
├── docs/                      # deep-dive documentation
│   ├── architecture.md        # capacity math and hardware limits
│   ├── api.md                 # REST API endpoints and payloads
│   └── security.md            # ssrf, csrf, bola mitigation details
├── src/
│   ├── main/
│   │   ├── java/com/shuu/berry/
│   │   │   ├── config/        # spring configuration files
│   │   │   ├── controller/    # rest controllers (auth, jobs, notifications)
│   │   │   ├── dto/           # request/response data transfer objects
│   │   │   ├── entity/        # jpa database entities (user, job, logs)
│   │   │   ├── notification/  # rabbitmq consumer and discord integration
│   │   │   ├── repository/    # database access layers
│   │   │   ├── security/      # jwt filters and ssrf URL validators
│   │   │   └── service/       # business logic (job scheduler, webhooks)
│   │   └── resources/
│   │       └── application.properties # global configuration settings
│   └── test/                  # unit and integration tests
├── pom.xml                    # maven dependency manager
└── README.md                  # this file
```

---

## Development

### Profile and Environment Setup
we use `APP_ENV` to change security behaviors between environments:
*   **dev**: disables strict HTTPS cookie checks and instantiates `UserController.java` (enables `GET /users` API for local testing).
*   **prod**: locks cookies to HTTPS only, adds sameSite attributes, and deletes the `/users` endpoint from memory.

### Running Tests
to execute the springboot test suite:
```sh
./mvnw test
```

---

## License

This project is licensed under the MIT License.
