package com.example.demo.controller;

import com.example.demo.dto.ShortenUrlRequest;
import com.example.demo.dto.ShortenUrlResponse;
import com.example.demo.model.ShortUrl;
import com.example.demo.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService service;

    @PostMapping("/api/v1/shorten")
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
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = service.resolveShortCode(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
