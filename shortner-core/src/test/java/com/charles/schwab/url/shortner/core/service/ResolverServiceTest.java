package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.common.exception.UrlNotFoundException;
import com.charles.schwab.url.shortner.core.entity.UrlMapping;
import com.charles.schwab.url.shortner.core.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolverServiceTest {

    @Mock
    private UrlMappingRepository repository;

    private ResolverService service;

    @BeforeEach
    void setUp() {
        service = new ResolverService(repository);
    }

    @Test
    void testResolveValidAndActive() {
        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl("https://valid.com");
        mapping.setIsActive("Y");
        // No expiry set
        
        when(repository.findByShortCode("good")).thenReturn(Optional.of(mapping));
        
        assertEquals("https://valid.com", service.resolve("good"));
    }

    @Test
    void testResolveNotFoundThrowsException() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());
        
        assertThrows(UrlNotFoundException.class, () -> service.resolve("unknown"));
    }

    @Test
    void testResolveInactiveThrowsException() {
        UrlMapping mapping = new UrlMapping();
        mapping.setIsActive("N");
        when(repository.findByShortCode("inactive")).thenReturn(Optional.of(mapping));
        
        UrlNotFoundException ex = assertThrows(UrlNotFoundException.class, () -> service.resolve("inactive"));
        assertEquals("URL not found or inactive for code: inactive", ex.getMessage());
    }

    @Test
    void testResolveExpiredThrowsException() {
        UrlMapping mapping = new UrlMapping();
        mapping.setIsActive("Y");
        // Expired yesterday
        mapping.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        when(repository.findByShortCode("expired")).thenReturn(Optional.of(mapping));
        
        UrlNotFoundException ex = assertThrows(UrlNotFoundException.class, () -> service.resolve("expired"));
        assertEquals("URL has expired for code: expired", ex.getMessage());
    }
}
