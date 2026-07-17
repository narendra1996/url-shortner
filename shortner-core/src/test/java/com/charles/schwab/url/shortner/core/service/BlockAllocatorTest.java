package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.core.entity.IdAllocation;
import com.charles.schwab.url.shortner.core.repository.IdAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockAllocatorTest {

    @Mock
    private IdAllocationRepository repository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private BlockAllocator blockAllocator;

    @BeforeEach
    void setUp() {
        blockAllocator = new BlockAllocator(repository, transactionTemplate);

        // Mock transaction template to execute the callback immediately
        doAnswer(invocation -> {
            Consumer<?> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void testAllocateBlockWhenEmpty() {
        when(repository.findByAllocatorNameForUpdate(anyString())).thenReturn(null);

        long id1 = blockAllocator.getNextId();
        long id2 = blockAllocator.getNextId();

        assertEquals(100000L, id1);
        assertEquals(100001L, id2);

        // Verify save was called twice: once for creation, once for block update
        verify(repository, times(2)).save(any(IdAllocation.class));
    }

    @Test
    void testAllocateBlockWhenExists() {
        IdAllocation existing = new IdAllocation();
        existing.setAllocatorName("url_shortner");
        existing.setCurrentMax(50000L);

        when(repository.findByAllocatorNameForUpdate(anyString())).thenReturn(existing);

        long id1 = blockAllocator.getNextId();
        long id2 = blockAllocator.getNextId();

        assertEquals(50000L, id1);
        assertEquals(50001L, id2);

        verify(repository, times(1)).save(any(IdAllocation.class));
    }

    @Test
    void testConcurrency() throws InterruptedException {
        IdAllocation existing = new IdAllocation();
        existing.setAllocatorName("url_shortner");
        existing.setCurrentMax(1000L);

        // Every time it's called, update existing's currentMax and return it to simulate DB behavior
        when(repository.findByAllocatorNameForUpdate(anyString())).thenAnswer(invocation -> existing);
        when(repository.save(any(IdAllocation.class))).thenAnswer(invocation -> null);

        int numberOfThreads = 50;
        int requestsPerThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        Set<Long> generatedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        generatedIds.add(blockAllocator.getNextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // 50 threads * 100 requests = 5000 unique IDs
        assertEquals(5000, generatedIds.size());
        
        // At least 5 block allocations (since block size is 1000)
        verify(repository, atLeast(5)).findByAllocatorNameForUpdate(anyString());
    }
}
