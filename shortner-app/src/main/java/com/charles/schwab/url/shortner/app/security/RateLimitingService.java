package com.charles.schwab.url.shortner.app.security;

public interface RateLimitingService {
    
    /**
     * Checks if the given client (identified by IP or user ID) has exceeded their rate limit.
     * @param clientId the unique identifier of the client (e.g. IP address or X-User-Id)
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    boolean tryConsume(String clientId);
}
