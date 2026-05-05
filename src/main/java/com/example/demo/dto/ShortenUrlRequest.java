package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for shortening a URL")
public class ShortenUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format")
    @Schema(description = "The original long URL to be shortened", example = "https://www.google.com")
    private String url;
    
    @Schema(description = "Optional expiration date and time for the short link", example = "2026-12-31T23:59:59Z")
    private OffsetDateTime expiresAt;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,10}$", message = "Custom alias must be 3–10 characters and contain only letters, digits, hyphens, or underscores")
    @Schema(description = "Optional custom alias for the short URL (3–10 alphanumeric/hyphen/underscore chars)", example = "my-link")
    private String customAlias;
}
