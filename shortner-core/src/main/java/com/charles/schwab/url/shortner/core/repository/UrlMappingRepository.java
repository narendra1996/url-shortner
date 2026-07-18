package com.charles.schwab.url.shortner.core.repository;

import com.charles.schwab.url.shortner.core.entity.UrlMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UrlMappingRepository extends CrudRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    Optional<UrlMapping> findByIdempotencyKeyAndUserId(String idempotencyKey, String userId);
    Page<UrlMapping> findByUserIdAndIsActive(String userId, String isActive, Pageable pageable);
}
