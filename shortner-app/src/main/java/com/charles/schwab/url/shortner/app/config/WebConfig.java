package com.charles.schwab.url.shortner.app.config;

import com.charles.schwab.url.shortner.app.security.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @NonNull
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(@NonNull RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Apply rate limiting primarily to the shorten creation API to prevent ID exhaustion
        // and management APIs to prevent scraping.
        // We might not rate limit the public redirect `{shortCode}` endpoint natively if we expect
        // a CDN/Edge to handle that scale, but we can include it here for local safety.
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**", "/*") // applies to all short code resolution as well
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**");
    }
}
