package com.example.demo.service;

import com.example.demo.exception.CustomAliasAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ShortCodeGenerationException;
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

    // ── random alias (existing behaviour) ────────────────────────────────────

    @Test
    void shortenUrl_ShouldSaveAndReturnShortUrl() {
        when(generator.generate()).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, null, null);

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
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        service.shortenUrl(originalUrl, null, null);

        verify(generator, times(2)).generate();
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void shortenUrl_ShouldThrowAfterMaxGenerationAttempts() {
        when(generator.generate()).thenReturn("sameCode");
        when(repository.existsByShortCode("sameCode")).thenReturn(true);

        assertThatThrownBy(() -> service.shortenUrl(originalUrl, null, null))
                .isInstanceOf(ShortCodeGenerationException.class);
    }

    @Test
    void shortenUrl_ShouldUseRandomCode_WhenAliasIsBlank() {
        when(generator.generate()).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, null, "  ");

        assertThat(result.getShortCode()).isEqualTo(shortCode);
        verify(generator).generate();
    }

    @Test
    void shortenUrl_ShouldPersistExpiresAt() {
        OffsetDateTime expiry = OffsetDateTime.now().plusDays(7);
        when(generator.generate()).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, expiry, null);

        assertThat(result.getExpiresAt()).isEqualTo(expiry);
    }

    // ── custom alias ──────────────────────────────────────────────────────────

    @Test
    void shortenUrl_ShouldUseCustomAlias_WhenAvailable() {
        String alias = "my-link";
        when(repository.existsByShortCode(alias)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, null, alias);

        assertThat(result.getShortCode()).isEqualTo(alias);
        verify(generator, never()).generate();
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void shortenUrl_ShouldThrowConflict_WhenCustomAliasAlreadyTaken() {
        String alias = "taken";
        when(repository.existsByShortCode(alias)).thenReturn(true);

        assertThatThrownBy(() -> service.shortenUrl(originalUrl, null, alias))
                .isInstanceOf(CustomAliasAlreadyExistsException.class)
                .hasMessageContaining("taken");

        verify(repository, never()).save(any());
    }

    @Test
    void shortenUrl_ShouldUseCustomAlias_WithExpiresAt() {
        String alias = "promo";
        OffsetDateTime expiry = OffsetDateTime.now().plusDays(30);
        when(repository.existsByShortCode(alias)).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.shortenUrl(originalUrl, expiry, alias);

        assertThat(result.getShortCode()).isEqualTo(alias);
        assertThat(result.getExpiresAt()).isEqualTo(expiry);
    }

    // ── resolve ───────────────────────────────────────────────────────────────

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

    @Test
    void resolveShortCode_ShouldSucceed_WhenNotExpired() {
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build();
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(shortUrl));

        String result = service.resolveShortCode(shortCode);

        assertThat(result).isEqualTo(originalUrl);
    }
}
