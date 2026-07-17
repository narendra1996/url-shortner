package com.charles.schwab.url.shortner.analytics.service;

import com.charles.schwab.url.shortner.analytics.repository.ClickEventRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }

    public long getClickCount(String shortCode) {
        return clickEventRepository.countByShortCode(shortCode);
    }
}
