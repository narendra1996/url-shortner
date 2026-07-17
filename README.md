# Charles Schwab - Premium URL Shortener

A high-performance, production-grade URL Shortener designed as a Maven multi-module Spring Boot application. 

## 🏗 Project Architecture

This project strictly adheres to separation of concerns via a multi-module architecture:

*   **[shortner-common](shortner-common)**: Base62 Codec, shared DTOs, custom exceptions, and common interfaces.
*   **[shortner-core](shortner-core)**: Core business logic (`ShortenerService`, `ResolverService`), repository layers, block allocation mechanism, and Liquibase database migrations.
*   **[shortner-analytics](shortner-analytics)**: Click event tracking decoupled via Spring Application Events (`@Async` listeners) to avoid impacting redirect latency.
*   **[shortner-app](shortner-app)**: Spring Boot bootstrapping, REST controllers, Spring MVC Configurations, Rate Limiting interceptors, and the Frontend UI.

---

## ✨ Features

- **Blazing Fast Redirects**: Achieved through an efficient `BlockAllocator` (AtomicLong ranges loaded into memory) combined with Caffeine L1 caching.
- **Asynchronous Analytics**: Click events are captured completely off the main thread using `@TransactionalEventListener` and Spring `@Async`.
- **Bot Mitigation**: Click tracking silently ignores common crawler user-agents (Googlebot, Discord, WhatsApp) to keep metrics pristine.
- **Beautiful UI**: A premium Dark Mode Single Page Application (SPA) utilizing Glassmorphism design principles, built natively in `src/main/resources/static` without the need for external Node.js dev servers.
- **Ready for Production**: Comes with a `docker-compose.yml` for evaluating the `prod` profile backed by PostgreSQL and Redis.
- **Robust Security**: Implement local API rate limiting to prevent enumeration and spam.

---

## 🚀 Getting Started

### 1. Build the Project

Use Maven Wrapper (included) to build the multi-module project cleanly without requiring a local Maven installation:

```bash
./mvnw clean install
```

### 2. Run the Application

Execute the packaged JAR directly:

```bash
java -jar shortner-app/target/shortner-app-0.0.1-SNAPSHOT.jar
```

*By default, the application runs on the `default` profile which uses an in-memory H2 database (wiped on restart) and Caffeine caching for frictionless local testing.*

---

## 💻 User Interface (UI)

Once the application is running, navigate to:

👉 **[http://localhost:8080/shorten-url](http://localhost:8080/shorten-url)**

- The UI will automatically generate a session identifier for you (`X-User-Id`) to track your shortening history.
- **Features**: Real-time click metric dashboard, one-click copy, and short link deletion.
- Note: Visiting the root path (`/`) will seamlessly redirect you to the `/shorten-url` dashboard.

---

## 🛠 Testing Production Dependencies

To evaluate the system using actual production infrastructure (PostgreSQL & Redis), you can spin up the provided Docker Compose stack:

```bash
# Start Infra
docker-compose up -d

# Run the app with the 'prod' profile
./mvnw spring-boot:run -pl shortner-app -Dspring-boot.run.profiles=prod
```

---

## ⚖️ Architecture Tradeoffs & Design Decisions

1. **H2 Default Over Testcontainers**: For the purpose of this assignment, the default profile relies on H2 to ensure the evaluator can simply compile and run the application without installing Docker. 
2. **Standard POJOs Over Lombok**: Lombok 1.18.x has known compilation failures on the bleeding-edge Java 25 JDK. To guarantee zero friction for the reviewer, all entities and DTOs were refactored to standard getters/setters/constructors.
3. **Block Allocation Algorithm**: Rather than relying on DB auto-increments or random string generation (which risks collisions), this application uses a distributed `IdAllocator`. The application reserves a "block" of 1,000 IDs at a time from the database using a pessimistic write lock (`FOR UPDATE`), and issues them from memory via `AtomicLong`. This allows millions of URLs to be shortened per minute with virtually no database contention.
4. **Decoupled Analytics**: Capturing analytics directly inside the redirect controller adds latency. By publishing a `UrlClickedEvent`, we immediately issue an HTTP 302 to the user, while a background thread computes the IP Hash and commits the click metrics to the database.
5. **Static UI Integration**: To avoid the complexity of starting a separate React/Vue Node server, the frontend is built using Vanilla JS/CSS and served natively by Spring Boot's static resource handler. This fits the "zip and email" assignment requirement perfectly while maintaining a premium SPA feel.
