package com.charles.schwab.url.shortner.app.security;

import com.charles.schwab.url.shortner.common.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Exclude actuator health checks or swagger if needed, but typically we map interceptors to specific paths
        
        String clientId = getClientId(request);
        
        if (!rateLimitingService.tryConsume(clientId)) {
            throw new TooManyRequestsException("You have exceeded your API rate limits.");
        }
        
        return true;
    }

    private String getClientId(HttpServletRequest request) {
        // Prefer explicit User ID header, fallback to IP Address for anonymous users
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank() && !userId.equals("anonymous")) {
            return "user:" + userId;
        }

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return "ip:" + request.getRemoteAddr();
        }
        return "ip:" + xfHeader.split(",")[0];
    }
}
