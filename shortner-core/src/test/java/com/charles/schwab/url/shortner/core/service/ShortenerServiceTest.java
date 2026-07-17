package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.common.dto.ShortenRequest;
import com.charles.schwab.url.shortner.common.dto.ShortenResponse;
import com.charles.schwab.url.shortner.common.exception.UrlNotFoundException;
import com.charles.schwab.url.shortner.core.entity.UrlMapping;
import com.charles.schwab.url.shortner.core.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortenerServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private BlockAllocator blockAllocator;

    @Mock
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    private ShortenerService service;

    @BeforeEach
    void setUp() {
        service = new ShortenerService(repository, blockAllocator, jdbcAggregateTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "domain", "http://localhost:8080/");
    }

    @Test
    void testShortenWithIdempotencyHit() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://google.com");
        request.setIdempotencyKey("test-key");

        UrlMapping existing = new UrlMapping();
        existing.setShortCode("exist");
        
        when(repository.findByIdempotencyKeyAndUserId("test-key", "user-1")).thenReturn(Optional.of(existing));

        ShortenResponse response = service.shorten(request, "user-1");

        assertEquals("http://localhost:8080/exist", response.getShortUrl());
        assertEquals("exist", response.getShortCode());
        
        // Ensure no new allocation or save was performed
        verify(blockAllocator, never()).getNextId();
        verify(jdbcAggregateTemplate, never()).insert(any(UrlMapping.class));
    }

    @Test
    void testShortenNewAllocation() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://google.com");
        
        when(blockAllocator.getNextId()).thenReturn(10L); // 10 encodes to 'A' in our Base62Codec
        when(jdbcAggregateTemplate.insert(any(UrlMapping.class))).thenReturn(new UrlMapping());

        ShortenResponse response = service.shorten(request, "user-1");

        assertEquals("A", response.getShortCode());
        
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(jdbcAggregateTemplate).insert(captor.capture());
        
        UrlMapping saved = captor.getValue();
        assertEquals(10L, saved.getId());
        assertEquals("A", saved.getShortCode());
        assertEquals("user-1", saved.getUserId());
        assertEquals("https://google.com", saved.getLongUrl());
        assertEquals("Y", saved.getIsActive());
    }

    @Test
    void testGetMetadataSuccess() {
        UrlMapping mapping = new UrlMapping();
        when(repository.findByShortCode("code")).thenReturn(Optional.of(mapping));
        
        assertEquals(mapping, service.getMetadata("code"));
    }

    @Test
    void testGetMetadataNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());
        
        assertThrows(UrlNotFoundException.class, () -> service.getMetadata("unknown"));
    }

    @Test
    void testGetUserUrls() {
        Page<UrlMapping> mockPage = new PageImpl<>(List.of(new UrlMapping()));
        when(repository.findByUserId(eq("user-1"), any(Pageable.class))).thenReturn(mockPage);
        
        Page<UrlMapping> result = service.getUserUrls("user-1", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSoftDeleteSuccess() {
        UrlMapping mapping = new UrlMapping();
        mapping.setIsActive("Y");
        when(repository.findByShortCode("code")).thenReturn(Optional.of(mapping));
        
        service.softDelete("code");
        
        verify(repository).save(mapping);
        assertEquals("N", mapping.getIsActive());
    }

    @Test
    void testSoftDeleteNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());
        assertThrows(UrlNotFoundException.class, () -> service.softDelete("unknown"));
    }
}
