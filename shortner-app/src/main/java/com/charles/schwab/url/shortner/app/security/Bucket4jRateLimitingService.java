package com.charles.schwab.url.shortner.app.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("!cloud") // Use this implementation locally or when cloud API gateway isn't handling it
public class Bucket4jRateLimitingService implements RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean tryConsume(String clientId) {
        Bucket bucket = cache.computeIfAbsent(clientId, this::createNewBucket);
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket(String clientId) {
        // We can define limits: e.g. 20 requests per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
