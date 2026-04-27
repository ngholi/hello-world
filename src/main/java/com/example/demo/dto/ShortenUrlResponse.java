package com.example.demo.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponse {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private OffsetDateTime expiresAt;
}
