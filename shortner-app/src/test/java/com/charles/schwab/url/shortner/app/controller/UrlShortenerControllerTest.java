package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.common.dto.ShortenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml")
@AutoConfigureMockMvc
public class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFullUrlLifecycleAndAnalytics() throws Exception {
        // 1. Shorten a URL
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");
        request.setIdempotencyKey("test-key-123");

        String shortenResponse = mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andReturn().getResponse().getContentAsString();

        String shortCode = objectMapper.readTree(shortenResponse).get("shortCode").asText();

        // 2.5 Get QR Code
        mockMvc.perform(get("/api/v1/urls/" + shortCode + "/qr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().exists("Cache-Control"));

        // 2. Get Metadata Lookup
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.longUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.isActive").value("Y"));

        // 3. Resolve and Redirect (which increments analytics)
        mockMvc.perform(get("/" + shortCode)
                        .header("User-Agent", "Mozilla/5.0")
                        .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));

        // Wait a brief moment for the async event listener to persist click event
        Thread.sleep(100);

        // 4. Get Click Stats
        mockMvc.perform(get("/api/v1/analytics/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.totalClicks").value(1));

        // 5. List paginated user links
        mockMvc.perform(get("/api/v1/urls")
                        .param("userId", "user-123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].shortCode").value(shortCode))
                .andExpect(jsonPath("$.content[0].longUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 6. Soft delete link
        mockMvc.perform(delete("/api/v1/urls/" + shortCode))
                .andExpect(status().isNoContent());

        // 7. Verify metadata shows inactive status
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value("N"));

        // 8. Verify resolve redirect fails now and returns a structured error
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/" + shortCode));
    }
}
