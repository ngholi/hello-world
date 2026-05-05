package com.example.demo;

import com.example.demo.dto.ShortenUrlRequest;
import com.example.demo.dto.ShortenUrlResponse;
import com.example.demo.exception.ErrorResponse;
import com.example.demo.model.ShortUrl;
import com.example.demo.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UrlShortenerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @BeforeEach
    void setUp() {
        shortUrlRepository.deleteAll();
    }

    // ── random alias ──────────────────────────────────────────────────────────

    @Test
    void shouldShortenAndRedirectUrl() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .build();

        ResponseEntity<ShortenUrlResponse> shortenResponse = restTemplate.postForEntity(
                "/api/v1/shorten", request, ShortenUrlResponse.class);

        assertThat(shortenResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String shortCode = shortenResponse.getBody().getShortCode();
        assertThat(shortCode).hasSizeBetween(7, 8);

        ResponseEntity<Void> redirectResponse = restTemplate.getForEntity(
                "/" + shortCode, Void.class);

        assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnGone_WhenUrlExpired() {
        ShortUrl expiredUrl = ShortUrl.builder()
                .shortCode("expired1")
                .originalUrl("https://expired.com")
                .createdAt(OffsetDateTime.now().minusDays(2))
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();
        shortUrlRepository.save(expiredUrl);

        ResponseEntity<Object> response = restTemplate.getForEntity("/expired1", Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
    }

    @Test
    void shouldReturnNotFound_WhenShortCodeDoesNotExist() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/nonexistent", Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnBadRequest_WhenUrlIsInvalid() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("not-a-url")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── custom alias ──────────────────────────────────────────────────────────

    @Test
    void shouldShortenWithCustomAlias() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("my-promo")
                .build();

        ResponseEntity<ShortenUrlResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ShortenUrlResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getShortCode()).isEqualTo("my-promo");
        assertThat(response.getBody().getShortUrl()).endsWith("/my-promo");
    }

    @Test
    void shouldRedirectCustomAlias() {
        shortUrlRepository.save(ShortUrl.builder()
                .shortCode("docs")
                .originalUrl("https://example.com")
                .build());

        ResponseEntity<Void> response = restTemplate.getForEntity("/docs", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnConflict_WhenCustomAliasAlreadyTaken() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("clash")
                .build();

        restTemplate.postForEntity("/api/v1/shorten", request, ShortenUrlResponse.class);

        ResponseEntity<ErrorResponse> secondResponse = restTemplate.postForEntity(
                "/api/v1/shorten", request, ErrorResponse.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(secondResponse.getBody().getMessage()).contains("clash");
    }

    @Test
    void shouldReturnBadRequest_WhenCustomAliasTooShort() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("ab")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequest_WhenCustomAliasTooLong() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("thiiswaytoolong")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequest_WhenCustomAliasHasInvalidChars() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("bad alias!")
                .build();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldShortenWithCustomAlias_AndExpiresAt() {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("temp")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .build();

        ResponseEntity<ShortenUrlResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten", request, ShortenUrlResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getShortCode()).isEqualTo("temp");
        assertThat(response.getBody().getExpiresAt()).isNotNull();
    }
}
