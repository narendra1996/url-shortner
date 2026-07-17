package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.analytics.service.AnalyticsService;
import com.charles.schwab.url.shortner.common.dto.ClickStatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class UrlAnalyticsController {

    private final AnalyticsService analyticsService;

    public UrlAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ClickStatsResponse> getClickStats(@PathVariable String shortCode) {
        long clickCount = analyticsService.getClickCount(shortCode);
        ClickStatsResponse response = new ClickStatsResponse(shortCode, clickCount);
        return ResponseEntity.ok(response);
    }
}
