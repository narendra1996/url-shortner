package com.charles.schwab.url.shortner.analytics.service;

import com.charles.schwab.url.shortner.analytics.event.UrlClickedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClickTrackingServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClickTrackingService service;

    @BeforeEach
    void setUp() {
        service = new ClickTrackingService(eventPublisher);
    }

    @Test
    void testTrackClickIgnoresBot() {
        // These user agents should be ignored by the regex
        String[] botAgents = {
                "Googlebot/2.1 (+http://www.google.com/bot.html)",
                "WhatsApp/2.21.12.21 A",
                "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)",
                "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
                "crawler"
        };

        for (String bot : botAgents) {
            service.trackClick("code", bot, "127.0.0.1");
        }

        // Verify event was NEVER published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testTrackClickPublishesEvent() {
        String validAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        String ipAddress = "192.168.1.1";

        service.trackClick("code123", validAgent, ipAddress);

        ArgumentCaptor<UrlClickedEvent> captor = ArgumentCaptor.forClass(UrlClickedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UrlClickedEvent event = captor.getValue();
        assertEquals("code123", event.getShortCode());
        assertEquals(validAgent, event.getUserAgent());
        assertNotNull(event.getIpHash());
        // Simple assertion to check it is hashed
        assertEquals(64, event.getIpHash().length());
    }

    @Test
    void testTrackClickWithNullUserAgent() {
        String ipAddress = "192.168.1.1";

        // Should still track if user agent is missing
        service.trackClick("code123", null, ipAddress);

        ArgumentCaptor<UrlClickedEvent> captor = ArgumentCaptor.forClass(UrlClickedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UrlClickedEvent event = captor.getValue();
        assertEquals("code123", event.getShortCode());
        assertEquals(null, event.getUserAgent());
    }
}
