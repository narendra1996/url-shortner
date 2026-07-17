package com.charles.schwab.url.shortner.analytics.listener;

import com.charles.schwab.url.shortner.analytics.entity.ClickEvent;
import com.charles.schwab.url.shortner.analytics.event.UrlClickedEvent;
import com.charles.schwab.url.shortner.analytics.repository.ClickEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventListenerTest {

    @Mock
    private ClickEventRepository clickEventRepository;

    private AnalyticsEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new AnalyticsEventListener(clickEventRepository);
    }

    @Test
    void testHandleUrlClickedEvent() {
        UrlClickedEvent event = new UrlClickedEvent("shortCode123", "Mozilla", "hash123");

        listener.handleUrlClickedEvent(event);

        ArgumentCaptor<ClickEvent> captor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(clickEventRepository).save(captor.capture());

        ClickEvent savedEvent = captor.getValue();
        assertEquals("shortCode123", savedEvent.getShortCode());
        assertEquals("Mozilla", savedEvent.getUserAgent());
        assertEquals("hash123", savedEvent.getIpHash());
        assertNotNull(savedEvent.getClickedAt());
    }
}
