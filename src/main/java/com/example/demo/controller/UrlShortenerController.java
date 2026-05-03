package com.example.demo.controller;

import com.example.demo.dto.ShortenUrlRequest;
import com.example.demo.dto.ShortenUrlResponse;
import com.example.demo.model.ShortUrl;
import com.example.demo.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "Endpoints for creating and resolving short URLs")
public class UrlShortenerController {

    private final UrlShortenerService service;

    @PostMapping("/api/v1/shorten")
    @Operation(summary = "Shorten a URL", description = "Creates a short code for a given long URL")
    @ApiResponse(responseCode = "201", description = "URL successfully shortened")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest servletRequest) {
        
        ShortUrl shortUrl = service.shortenUrl(request.getUrl(), request.getExpiresAt());
        
        String baseUrl = servletRequest.getRequestURL().toString().replace(servletRequest.getRequestURI(), "");
        String fullShortUrl = baseUrl + "/" + shortUrl.getShortCode();
        
        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(fullShortUrl)
                .originalUrl(shortUrl.getOriginalUrl())
                .expiresAt(shortUrl.getExpiresAt())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL", description = "Resolves the short code and redirects to the original long URL")
    @ApiResponse(responseCode = "302", description = "Redirected successfully")
    @ApiResponse(responseCode = "404", description = "Short code not found")
    @ApiResponse(responseCode = "410", description = "Short code has expired")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = service.resolveShortCode(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
