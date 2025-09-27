# Spring Web API

A minimal Spring Boot web API built with:
- Java 24
- Spring MVC (REST)
- Spring Data JPA
- Jakarta EE (jakarta.* imports)
- Gradle

## Getting Started

### Prerequisites
- Java 24 (JDK 24)
- Gradle (wrapper included)

### Build
- Linux/macOS: `./gradlew clean build`
- Windows: `gradlew clean build`

Artifacts are produced under `build/`.

### Run
- Linux/macOS: `./gradlew bootRun`
- Windows: `gradlew bootRun`

The API will start on `http://localhost:8080`.

### Test
- `./gradlew test`

## Project Structure
- `src/main/java` — application and REST controllers
- `src/main/resources` — application configuration (e.g., application.yml/properties)
- `src/test/java` — tests
- `build.gradle` — dependencies and build configuration
- `settings.gradle` — project settings

## Configuration
Common properties (in `src/main/resources/application.yml` or `.properties`):
- `server.port` — server port (default 8080)
- `spring.datasource.*` — database connection
- `spring.jpa.*` — JPA/Hibernate settings

Example (YAML):