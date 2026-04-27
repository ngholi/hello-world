package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ShortCodeGenerationException;
import com.example.demo.exception.UrlExpiredException;
import com.example.demo.model.ShortUrl;
import com.example.demo.repository.ShortUrlRepository;
import com.example.demo.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {
    private static final int MAX_SHORT_CODE_GENERATION_ATTEMPTS = 10;

    private final ShortUrlRepository repository;
    private final Base62Generator generator;

    @Transactional
    public ShortUrl shortenUrl(String originalUrl, OffsetDateTime expiresAt) {
        String shortCode = generateUniqueShortCode();
        
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .expiresAt(expiresAt)
                .build();
        
        log.info("Creating short URL for {}: {}", originalUrl, shortCode);
        return repository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    public String resolveShortCode(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code not found: " + shortCode));
        
        if (shortUrl.isExpired()) {
            log.warn("Attempted to access expired short code: {}", shortCode);
            throw new UrlExpiredException("Short code has expired: " + shortCode);
        }
        
        return shortUrl.getOriginalUrl();
    }

    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        do {
            shortCode = generator.generate();
            attempts++;
            if (attempts > MAX_SHORT_CODE_GENERATION_ATTEMPTS) {
                throw new ShortCodeGenerationException("Failed to generate unique short code after 10 attempts");
            }
        } while (repository.existsByShortCode(shortCode));
        return shortCode;
    }
}
