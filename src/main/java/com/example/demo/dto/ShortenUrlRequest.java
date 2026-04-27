package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format")
    private String url;
    
    private OffsetDateTime expiresAt;
}
