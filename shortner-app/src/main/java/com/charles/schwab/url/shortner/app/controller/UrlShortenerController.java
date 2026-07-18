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
    private final com.charles.schwab.url.shortner.core.service.QrCodeService qrCodeService;

    @org.springframework.beans.factory.annotation.Value("${app.domain:http://localhost:8080/}")
    private String domain;

    public UrlShortenerController(ShortenerService shortenerService, 
                                  com.charles.schwab.url.shortner.core.service.QrCodeService qrCodeService) {
        this.shortenerService = shortenerService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request,
                                                      @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId) {
        ShortenResponse response = shortenerService.shorten(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/api/v1/urls/{shortCode}/qr", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortCode) {
        // verify it exists and is active (throws UrlNotFoundException if not)
        shortenerService.getMetadata(shortCode); 
        byte[] qrImage = qrCodeService.generateQrCode(domain + shortCode);
        return ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.maxAge(7, java.util.concurrent.TimeUnit.DAYS))
                .body(qrImage);
    }
}
