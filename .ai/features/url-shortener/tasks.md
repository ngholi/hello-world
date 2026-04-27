# Implementation Tasks: URL Shortener Feature

## Stage 1: Infrastructure & Environment Setup
1.  **Dependency Updates:**
    *   Add `spring-boot-starter-data-jpa` to `pom.xml`.
    *   Add `postgresql` driver dependency to `pom.xml`.
    *   (Optional) Add `validation` starter for API request validation.
2.  **Configuration:**
    *   Configure PostgreSQL connection details in `application.yml`.
    *   **Database Schema:** Create SQL migration scripts for any schema changes (stored in `.ai/features/url-shortener/db/`).
    *   Ensure `ddl-auto` is set to `none` or `validate`.

## Stage 2: Domain Model & Persistence
3.  **Entity Implementation:**
    *   Create `ShortUrl` entity with fields: `id`, `shortCode`, `originalUrl`, `createdAt`, `expiresAt`.
    *   Add appropriate JPA annotations (Table name, Unique constraints, Indices).
4.  **Repository Layer:**
    *   Create `ShortUrlRepository` extending `JpaRepository`.
    *   Implement method `findByShortCode(String shortCode)`.

## Stage 3: Core Logic & Services
5.  **Short Code Generator:**
    *   Implement a utility or service to generate 7-8 character Base62 strings.
    *   Handle potential collisions (check if code exists in DB).
6.  **URL Service:**
    *   Implement `shortenUrl(LongUrlRequest request)` logic.
    *   Implement `resolveShortCode(String shortCode)` logic with expiration check.

## Stage 4: API Layer
7.  **DTOs:**
    *   Create `ShortenUrlRequest` and `ShortenUrlResponse`.
8.  **REST Controllers:**
    *   Implement `POST /api/v1/shorten` in `UrlShortenerController`.
    *   Implement `GET /{shortCode}` for redirection.
9.  **Global Exception Handling:**
    *   Handle "Not Found" and "Expired" scenarios with appropriate HTTP status codes (404/410).

## Stage 5: Verification & Testing
10. **Unit Tests:**
    *   Test Base62 generation logic.
    *   Test service logic (creation and expiration).
11. **Integration Tests:**
    *   Verify API endpoints with a test database using Testcontainers.
    *   Verify redirection behavior and expiration logic.
