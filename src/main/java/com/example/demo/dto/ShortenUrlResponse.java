package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a shortened URL")
public class ShortenUrlResponse {
    @Schema(description = "The generated unique short code", example = "aB12cd")
    private String shortCode;
    
    @Schema(description = "The full short URL for redirection", example = "http://localhost:8080/aB12cd")
    private String shortUrl;
    
    @Schema(description = "The original long URL", example = "https://www.google.com")
    private String originalUrl;
    
    @Schema(description = "The date and time when the short link expires", example = "2026-12-31T23:59:59Z")
    private OffsetDateTime expiresAt;
}
