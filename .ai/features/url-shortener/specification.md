# Specification: URL Shortener Feature

## 1. Overview
The URL Shortener is a service that takes a long URL and generates a concise, unique "short code". When a user visits the short URL, they are redirected to the original long URL.

## 2. Requirements

### 2.1 Functional Requirements
- **Generate Short URL:**
    - User provides a long URL.
    - User optionally provides an expiration date (`expiresAt`).
    - System returns a unique short URL using a 7-8 character code (Base62).
- **Redirect to Long URL:**
    - System redirects short URL visits to the original destination.
    - Redirection type: **HTTP 302 (Found)**.
- **Expiration:**
    - If a link has an expiration date and it has passed, the system should return **HTTP 410 (Gone)** or **HTTP 404 (Not Found)**.
    - If no expiration is set, the link remains active indefinitely.
- **Persistence:**
    - All data must be stored in a **PostgreSQL** database.

### 2.2 Technical Requirements
- **Framework:** Spring Boot.
- **Data Access:** Spring Data JPA.
- **Database:** PostgreSQL.
- **Short Code Generation:** Base62 encoding (using characters `[a-z, A-Z, 0-9]`).

## 3. API Design

### 3.1 Create Short URL
- **Endpoint:** `POST /api/v1/shorten`
- **Request Body:**
  ```json
  {
    "url": "https://www.extremely-long-url.com/some/deep/path?query=params",
    "expiresAt": "2026-12-31T23:59:59Z" (Optional)
  }
  ```
- **Response Body:**
  ```json
  {
    "shortCode": "aB34zXy",
    "shortUrl": "http://domain.com/aB34zXy",
    "originalUrl": "https://www.extremely-long-url.com/some/deep/path?query=params",
    "expiresAt": "2026-12-31T23:59:59Z"
  }
  ```

### 3.2 Redirection
- **Endpoint:** `GET /{shortCode}`
- **Behavior:**
    - Finds the original URL associated with `shortCode`.
    - Checks expiration.
    - Returns **302 Redirect** if valid.

## 4. Data Model

### 4.1 ShortUrl Entity
- `id`: Long (Primary Key)
- `shortCode`: String (Unique, Indexed, 7-8 characters)
- `originalUrl`: String (Text, non-nullable)
- `createdAt`: Timestamp
- `expiresAt`: Timestamp (Nullable)

## 5. Future Considerations (Out of Scope for now)
- User authentication/ownership of links.
- Analytics/Click tracking (Geographic, browser, etc.).
- Custom aliases.
