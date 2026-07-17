package com.charles.schwab.url.shortner.analytics.service;

import com.charles.schwab.url.shortner.analytics.event.UrlClickedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ClickTrackingService {

    private final ApplicationEventPublisher eventPublisher;

    public ClickTrackingService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private static final String BOT_REGEX = "(?i).*(bot|crawler|spider|ping|slurp|facebookexternalhit|whatsapp|discord).*";

    public void trackClick(String shortCode, String userAgent, String ipAddress) {
        if (userAgent != null && userAgent.matches(BOT_REGEX)) {
            // Silently ignore bot traffic so it doesn't pollute analytics
            return;
        }

        String ipHash = hashIp(ipAddress);
        eventPublisher.publishEvent(new UrlClickedEvent(shortCode, userAgent, ipHash));
    }

    private String hashIp(String ip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ip.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}
