# Coding Principles

All code in this project must adhere to the following principles:

## 1. Clean Code & Readability
- **Naming:** Use descriptive names for variables, methods, and classes. Avoid abbreviations.
- **Small Methods:** Keep methods focused on a single responsibility.
- **Self-Documenting:** Code should be clear enough that comments are rarely needed to explain *what* is happening.

## 2. Java & Spring Boot Best Practices
- **Lombok:** Use Lombok annotations (`@Data`, `@Builder`, `@Slf4j`) to reduce boilerplate.
- **Constructor Injection:** Prefer constructor injection over `@Autowired` field injection.
- **Validation:** Use `jakarta.validation` for input data verification.
- **Immutable DTOs:** Request and Response objects should be immutable where possible.

## 3. Architecture
- **Layered Architecture:** Strictly follow the Controller -> Service -> Repository pattern.
- **Domain Focus:** Keep business logic inside the Service layer.
- **Global Exception Handling:** Use `@RestControllerAdvice` to manage API errors consistently.

## 4. Testing
- **Coverage:** Aim for at least **80% code coverage** for all new features.
- **Test-Driven:** Write tests for core business logic and edge cases (e.g., URL expiration).
- **Isolation:** Use `@Mock` for dependencies in unit tests.
- **Integration Tests:** Use `@SpringBootTest` with a test database for API-level verification.

## 5. Performance & Scalability
- **Indexing:** Ensure frequently searched columns (like `shortCode`) are indexed in the database.
- **Efficient Generation:** Use collision-resistant algorithms for unique ID/code generation.
