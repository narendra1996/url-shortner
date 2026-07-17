package com.charles.schwab.url.shortner.app.controller;

import com.charles.schwab.url.shortner.analytics.service.ClickTrackingService;
import com.charles.schwab.url.shortner.core.service.ResolverService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlRedirectController {

    private final ResolverService resolverService;
    private final ClickTrackingService clickTrackingService;

    public UrlRedirectController(ResolverService resolverService, ClickTrackingService clickTrackingService) {
        this.resolverService = resolverService;
        this.clickTrackingService = clickTrackingService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> resolveAndRedirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = resolverService.resolve(shortCode);

        // Track click asynchronously via the Tracking Service
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = getClientIp(request);
        clickTrackingService.trackClick(shortCode, userAgent, ipAddress);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
