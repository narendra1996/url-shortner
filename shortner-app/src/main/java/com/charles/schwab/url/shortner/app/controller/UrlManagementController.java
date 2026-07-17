package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.common.dto.UrlMetadataResponse;
import com.charles.schwab.url.shortner.core.entity.UrlMapping;
import com.charles.schwab.url.shortner.core.service.ShortenerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlManagementController {

    private final ShortenerService shortenerService;

    public UrlManagementController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlMetadataResponse> getUrlMetadata(@PathVariable String shortCode) {
        UrlMapping mapping = shortenerService.getMetadata(shortCode);
        UrlMetadataResponse response = new UrlMetadataResponse(
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getUserId(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt(),
                mapping.getIsActive()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UrlMetadataResponse>> getUserUrls(
            @RequestParam String userId,
            Pageable pageable) {
        Page<UrlMapping> mappings = shortenerService.getUserUrls(userId, pageable);
        Page<UrlMetadataResponse> response = mappings.map(mapping -> new UrlMetadataResponse(
                mapping.getShortCode(),
                mapping.getLongUrl(),
                mapping.getUserId(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt(),
                mapping.getIsActive()
        ));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        shortenerService.softDelete(shortCode);
        return ResponseEntity.noContent().build();
    }
}
