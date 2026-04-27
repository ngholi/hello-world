# Spring Boot Traceable Logging & Zipkin Integration

This project is a Spring Boot application demonstrating how to implement traceable logging using Logback and distributed tracing with Zipkin and Micrometer Tracing.

## Features

- **Standardized Error Handling**: Global exception handler returning structured JSON responses.
- **Traceable Logging**: Logback configured with Logstash encoder to include `traceId` and `spanId` in every log entry.
- **Distributed Tracing**: Integrated with Micrometer Tracing and OpenTelemetry to export traces to Zipkin.
- **Observability**: Uses `@Observed` annotation for automatic tracing of controller methods.

## Prerequisites

- Java 21
- Maven 3.x
- Docker (optional, for running Zipkin)

## Getting Started

### 1. Run Zipkin

If you want to view traces in the Zipkin UI, you can start Zipkin using Docker:

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

### 2. Configure the Application

The application is configured via `src/main/resources/application.yml`. By default, Zipkin export is disabled.

To enable Zipkin export, you can set environment variables or modify the properties:

```properties
ZIPKIN_ENABLED=true
ZIPKIN_URL=http://localhost:9411/api/v2/spans
```

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Docker Build and Run

You can also build and run the application as a Docker container:

```bash
# Build the Docker image
docker build -t hello-world-demo:1.0.9 .

# Run the container
docker run -p 8080:8080 hello-world-demo:1.0.9
```

## Traceable Logging

### Logback Configuration
The `src/main/resources/logback-spring.xml` is configured to output logs to both the console and a rolling file (`logs/spring-boot-logger.log`).

- **Console Pattern**: Includes `traceId` and `spanId`.
- **JSON Logging**: The file appender uses `LogstashEncoder` to produce JSON logs, making them easily searchable by log management systems like ELK.

Example log entry in `spring-boot-logger.log`:
```json
{
  "@timestamp": "2026-04-26T15:14:28.429844+07:00",
  "message": "Handling test-exception request",
  "logger_name": "com.example.demo.controller.HelloController",
  "level": "INFO",
  "mdc": {
    "traceId": "de8af13dc6b2970ce12471332849e2a5",
    "spanId": "f1835bb8a37759d4"
  }
}
```

## Distributed Tracing

### Setup
The project uses Micrometer Tracing with OpenTelemetry bridge:
- `micrometer-tracing-bridge-otel`: Bridge between Micrometer and OpenTelemetry.
- `opentelemetry-exporter-zipkin`: Exporter to send spans to Zipkin.

### Using @Observed
The `HelloController` is annotated with `@Observed`, which automatically creates spans for its methods:

```java
@RestController
@Observed(name = "hello.controller")
public class HelloController {
    // ...
}
```

## Verification

1. **Check Logs**: Access `http://localhost:8080/hello` or `http://localhost:8080/test-exception` and check the console or `logs/spring-boot-logger.log`. You should see `traceId` and `spanId`.
2. **Zipkin UI**: Open `http://localhost:9411` in your browser. Search for traces to see the call graphs and timing information for your requests.

## API Endpoints

- `GET /hello`: Returns "Hello world".
- `GET /test-exception`: Throws an exception to demonstrate error logging and tracing.
- `GET /actuator/health`: Health check endpoint.
