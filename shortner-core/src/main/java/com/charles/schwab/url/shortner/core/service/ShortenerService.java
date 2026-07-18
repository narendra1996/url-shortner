package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.common.dto.ShortenRequest;
import com.charles.schwab.url.shortner.common.dto.ShortenResponse;
import com.charles.schwab.url.shortner.common.exception.UrlNotFoundException;
import com.charles.schwab.url.shortner.common.util.Base62Codec;
import com.charles.schwab.url.shortner.core.entity.UrlMapping;
import com.charles.schwab.url.shortner.core.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShortenerService {
    private final UrlMappingRepository urlMappingRepository;
    private final BlockAllocator blockAllocator;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public ShortenerService(UrlMappingRepository urlMappingRepository,
                            BlockAllocator blockAllocator,
                            JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.urlMappingRepository = urlMappingRepository;
        this.blockAllocator = blockAllocator;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Value("${app.domain:http://localhost:8080/}")
    private String domain;

    public ShortenResponse shorten(ShortenRequest request, String userId) {
        if (request.getIdempotencyKey() != null) {
            var existing = urlMappingRepository.findByIdempotencyKeyAndUserId(request.getIdempotencyKey(), userId);
            if (existing.isPresent()) {
                String shortCode = existing.get().getShortCode();
                return new ShortenResponse(domain + shortCode, shortCode);
            }
        }

        long id = blockAllocator.getNextId();
        String shortCode = Base62Codec.encode(id);

        UrlMapping mapping = new UrlMapping();
        mapping.setId(id);
        mapping.setShortCode(shortCode);
        mapping.setLongUrl(request.getUrl());
        mapping.setUserId(userId);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setIsActive("Y");
        mapping.setIdempotencyKey(request.getIdempotencyKey());

        jdbcAggregateTemplate.insert(mapping);

        return new ShortenResponse(domain + shortCode, shortCode);
    }

    public UrlMapping getMetadata(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for code: " + shortCode));
    }

    public Page<UrlMapping> getUserUrls(String userId, Pageable pageable) {
        return urlMappingRepository.findByUserIdAndIsActive(userId, "Y", pageable);
    }

    @CacheEvict(value = "urlCache", key = "#shortCode")
    public void softDelete(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for code: " + shortCode));
        mapping.setIsActive("N");
        urlMappingRepository.save(mapping);
    }
}
