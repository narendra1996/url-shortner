package com.charles.schwab.url.shortner.analytics.listener;

import com.charles.schwab.url.shortner.analytics.entity.ClickEvent;
import com.charles.schwab.url.shortner.analytics.event.UrlClickedEvent;
import com.charles.schwab.url.shortner.analytics.repository.ClickEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);
    private final ClickEventRepository clickEventRepository;

    public AnalyticsEventListener(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleUrlClickedEvent(UrlClickedEvent event) {
        log.info("Processing click event asynchronously for shortCode: {}", event.getShortCode());
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setShortCode(event.getShortCode());
        clickEvent.setClickedAt(LocalDateTime.now());
        clickEvent.setUserAgent(event.getUserAgent());
        clickEvent.setIpHash(event.getIpHash());
        
        clickEventRepository.save(clickEvent);
    }
}
