package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.common.exception.UrlNotFoundException;
import com.charles.schwab.url.shortner.core.repository.UrlMappingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ResolverService {
    private final UrlMappingRepository urlMappingRepository;

    public ResolverService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @Cacheable(value = "urlCache", key = "#shortCode")
    public String resolve(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(mapping -> {
                    if (!"Y".equals(mapping.getIsActive())) {
                        throw new UrlNotFoundException("URL not found or inactive for code: " + shortCode);
                    }
                    if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                        throw new UrlNotFoundException("URL has expired for code: " + shortCode);
                    }
                    return mapping.getLongUrl();
                })
                .orElseThrow(() -> new UrlNotFoundException("URL not found or inactive for code: " + shortCode));
    }
}
