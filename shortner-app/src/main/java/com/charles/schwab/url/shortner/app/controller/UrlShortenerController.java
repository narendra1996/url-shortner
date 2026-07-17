package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.common.dto.ShortenRequest;
import com.charles.schwab.url.shortner.common.dto.ShortenResponse;
import com.charles.schwab.url.shortner.core.service.ShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortenerController {

    private final ShortenerService shortenerService;

    public UrlShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request,
                                                      @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
        ShortenResponse response = shortenerService.shorten(request, userId);
        return ResponseEntity.ok(response);
    }
}
