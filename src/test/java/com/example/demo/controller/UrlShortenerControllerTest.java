package com.example.demo.controller;

import com.example.demo.dto.ShortenUrlRequest;
import com.example.demo.exception.CustomAliasAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UrlExpiredException;
import com.example.demo.model.ShortUrl;
import com.example.demo.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService service;

    @Autowired
    private ObjectMapper objectMapper;

    // ── shorten — random alias ────────────────────────────────────────────────

    @Test
    void shortenUrl_ShouldReturnCreated() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .build();

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode("abcdefgh")
                .originalUrl("https://example.com")
                .build();

        when(service.shortenUrl(anyString(), any(), any())).thenReturn(shortUrl);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abcdefgh"))
                .andExpect(jsonPath("$.shortUrl").value(containsString("/abcdefgh")))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"));
    }

    @Test
    void shortenUrl_ShouldReturnBadRequest_WhenInvalidUrl() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("invalid-url")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenUrl_ShouldReturnBadRequest_WhenUrlIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── shorten — custom alias ────────────────────────────────────────────────

    @Test
    void shortenUrl_ShouldReturnCreated_WithCustomAlias() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("my-link")
                .build();

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode("my-link")
                .originalUrl("https://example.com")
                .build();

        when(service.shortenUrl(anyString(), any(), eq("my-link"))).thenReturn(shortUrl);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("my-link"))
                .andExpect(jsonPath("$.shortUrl").value(containsString("/my-link")));
    }

    @Test
    void shortenUrl_ShouldReturnBadRequest_WhenCustomAliasTooShort() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("ab")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("customAlias")));
    }

    @Test
    void shortenUrl_ShouldReturnBadRequest_WhenCustomAliasTooLong() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("thiiswaytoolong")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("customAlias")));
    }

    @Test
    void shortenUrl_ShouldReturnBadRequest_WhenCustomAliasHasInvalidChars() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("bad alias!")
                .build();

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("customAlias")));
    }

    @Test
    void shortenUrl_ShouldReturnConflict_WhenCustomAliasAlreadyTaken() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .customAlias("taken")
                .build();

        when(service.shortenUrl(anyString(), any(), eq("taken")))
                .thenThrow(new CustomAliasAlreadyExistsException("taken"));

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("taken")));
    }

    // ── redirect ──────────────────────────────────────────────────────────────

    @Test
    void redirect_ShouldReturnFound() throws Exception {
        when(service.resolveShortCode("abcdefgh")).thenReturn("https://example.com");

        mockMvc.perform(get("/abcdefgh"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void redirect_ShouldReturnNotFound_WhenCodeMissing() throws Exception {
        when(service.resolveShortCode("missing"))
                .thenThrow(new ResourceNotFoundException("Short code not found: missing"));

        mockMvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void redirect_ShouldReturnGone_WhenCodeExpired() throws Exception {
        when(service.resolveShortCode("expired"))
                .thenThrow(new UrlExpiredException("Short code has expired: expired"));

        mockMvc.perform(get("/expired"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.status").value(410));
    }
}
