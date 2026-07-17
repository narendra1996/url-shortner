# URL Shortener

A high-performance, multi-module Spring Boot URL Shortener project.

## Project Structure

This project is organized as a Maven multi-module project:

*   **[shortner-common](file:///Users/narendra/IdeaProjects/url-shortner/shortner-common)**: Holds shared data models, utilities, and common configurations.
*   **[shortner-core](file:///Users/narendra/IdeaProjects/url-shortner/shortner-core)**: Core business logic, URL shortening algorithm, and database storage.
*   **[shortner-analytics](file:///Users/narendra/IdeaProjects/url-shortner/shortner-analytics)**: Tracks URL access analytics and metrics.
*   **[shortner-app](file:///Users/narendra/IdeaProjects/url-shortner/shortner-app)**: Spring Boot entry point, REST controllers, and application configuration.

---

## Prerequisites

*   **Java 21** or higher.
*   **Maven** installed on your system path.

---

## Getting Started

### 1. Build the Project

Use Maven to build the multi-module project:

```bash
mvn clean install
```

### 2. Run the Application

#### A. Through Maven CLI

Run the Spring Boot application module:

```bash
mvn spring-boot:run -pl shortner-app
```

Or run the packaged jar:

```bash
java -jar shortner-app/target/shortner-app-0.0.1-SNAPSHOT.jar
```

The application starts by default at `http://localhost:8080`.

#### B. Through Antigravity

Antigravity can help you manage and run the application directly from the chat interface. You can instruct the agent to run the application using commands like:

*   *"Build and start the application"*
*   *"Run the tests"*

Antigravity will run the appropriate maven commands (`mvn clean install` followed by `mvn spring-boot:run -pl shortner-app`) in the background and notify you when the service is up.

---

## Verification

Once running, you can verify the application status:

*   **Health Check**: `http://localhost:8080/actuator/health`
*   **API Documentation (Swagger UI)**: `http://localhost:8080/swagger-ui/index.html`
