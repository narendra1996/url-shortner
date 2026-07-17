package com.charles.schwab.url.shortner.analytics.repository;

import com.charles.schwab.url.shortner.analytics.entity.ClickEvent;
import org.springframework.data.repository.CrudRepository;

public interface ClickEventRepository extends CrudRepository<ClickEvent, Long> {
    long countByShortCode(String shortCode);
}
