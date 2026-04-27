package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UrlExpiredException;
import com.example.demo.model.ShortUrl;
import com.example.demo.repository.ShortUrlRepository;
import com.example.demo.util.Base62Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private Base62Generator generator;

    @InjectMocks
    private UrlShortenerService service;

    private final String originalUrl = "https://example.com";
    private final String shortCode = "abcdefgh";

    @Test
    void shortenUrl_ShouldSaveAndReturnShortUrl() {
        when(generator.generate()).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, null);

        assertThat(result.getShortCode()).isEqualTo(shortCode);
        assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void shortenUrl_ShouldRetryOnCollision() {
        String code1 = "collision";
        String code2 = "uniqueee";
        when(generator.generate()).thenReturn(code1, code2);
        when(repository.existsByShortCode(code1)).thenReturn(true);
        when(repository.existsByShortCode(code2)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.shortenUrl(originalUrl, null);

        verify(generator, times(2)).generate();
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void resolveShortCode_ShouldReturnOriginalUrl() {
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .build();
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(shortUrl));

        String result = service.resolveShortCode(shortCode);

        assertThat(result).isEqualTo(originalUrl);
    }

    @Test
    void resolveShortCode_ShouldThrowNotFound() {
        when(repository.findByShortCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveShortCode("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveShortCode_ShouldThrowExpired() {
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> service.resolveShortCode(shortCode))
                .isInstanceOf(UrlExpiredException.class);
    }
}
