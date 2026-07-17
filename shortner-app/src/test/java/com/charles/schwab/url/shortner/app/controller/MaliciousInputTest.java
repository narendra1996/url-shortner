package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.common.dto.ShortenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml")
@AutoConfigureMockMvc
public class MaliciousInputTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testShortenUrlWithXssPayloadShouldFail() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("javascript:alert('XSS')");
        
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "malicious-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testShortenUrlWithLocalhostShouldFail() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("http://localhost:8080/admin");

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "malicious-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testShortenUrlWithInternalIpShouldFail() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("http://192.168.1.1/secret");

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "malicious-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testShortenUrlWithExtremelyLongUrlShouldFail() throws Exception {
        ShortenRequest request = new ShortenRequest();
        // Create a URL longer than 2048 characters
        StringBuilder sb = new StringBuilder("https://example.com/");
        for (int i = 0; i < 2100; i++) {
            sb.append("a");
        }
        request.setUrl(sb.toString());

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", "malicious-user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testRateLimiting() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://google.com");
        
        // The rate limit interceptor might allow 100 requests per minute or something similar.
        // For testing purposes, we'll hit it until we get a 429 Too Many Requests.
        boolean gotRateLimited = false;
        
        // Loop up to 150 times. If the rate limit is hit, break and assert true.
        for (int i = 0; i < 150; i++) {
            int status = mockMvc.perform(post("/api/v1/shorten")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("X-User-Id", "spammer-user"))
                    .andReturn().getResponse().getStatus();
                    
            if (status == 429) {
                gotRateLimited = true;
                break;
            }
        }
        
        // Assuming the rate limit interceptor is configured for max 100 requests, this should be true.
        org.junit.jupiter.api.Assertions.assertTrue(gotRateLimited, "Expected to receive HTTP 429 Too Many Requests");
    }
}
