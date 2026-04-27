package com.example.demo.controller;

import com.example.demo.dto.ShortenUrlRequest;
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

    @Test
    void shortenUrl_ShouldReturnCreated() throws Exception {
        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .url("https://example.com")
                .build();
        
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode("abcdefgh")
                .originalUrl("https://example.com")
                .build();
        
        when(service.shortenUrl(anyString(), any())).thenReturn(shortUrl);

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
    void redirect_ShouldReturnFound() throws Exception {
        when(service.resolveShortCode("abcdefgh")).thenReturn("https://example.com");

        mockMvc.perform(get("/abcdefgh"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }
}
