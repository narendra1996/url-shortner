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
        return urlMappingRepository.findByShortCode(shortCode).filter(mapping -> "Y".equals(mapping.getIsActive()))
                .map(mapping -> mapping.getLongUrl())
                .orElseThrow(() -> new UrlNotFoundException("URL not found or inactive for code: " + shortCode));
    }
}
