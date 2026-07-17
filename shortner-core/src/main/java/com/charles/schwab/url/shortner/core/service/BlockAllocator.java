package com.charles.schwab.url.shortner.core.service;

import com.charles.schwab.url.shortner.core.entity.IdAllocation;
import com.charles.schwab.url.shortner.core.repository.IdAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BlockAllocator {
    private final IdAllocationRepository repository;
    private final TransactionTemplate transactionTemplate;

    public BlockAllocator(IdAllocationRepository repository, TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
    }

    private static final String ALLOCATOR_NAME = "url_shortner";
    private static final long BLOCK_SIZE = 1000;

    private final AtomicLong currentId = new AtomicLong(0);
    private volatile long maxId = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public long getNextId() {
        while (true) {
            long id = currentId.getAndIncrement();
            if (id < maxId) {
                return id;
            }
            allocateBlock();
        }
    }

    private void allocateBlock() {
        lock.lock();
        try {
            if (currentId.get() >= maxId) {
                transactionTemplate.executeWithoutResult(status -> {
                    IdAllocation allocation = repository.findByAllocatorNameForUpdate(ALLOCATOR_NAME);
                    if (allocation == null) {
                        allocation = new IdAllocation();
                        allocation.setAllocatorName(ALLOCATOR_NAME);
                        allocation.setCurrentMax(100000L);
                        repository.save(allocation);
                    }

                    long start = allocation.getCurrentMax();
                    long end = start + BLOCK_SIZE;

                    allocation.setCurrentMax(end);
                    repository.save(allocation);

                    currentId.set(start);
                    maxId = end;
                });
            }
        } finally {
            lock.unlock();
        }
    }
}
