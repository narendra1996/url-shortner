package com.charles.schwab.url.shortner.common.dto;

import com.charles.schwab.url.shortner.common.validation.SafeUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShortenRequest {
    
    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    @SafeUrl
    private String url;

    @Size(max = 128, message = "Idempotency key cannot exceed 128 characters")
    private String idempotencyKey;

    public ShortenRequest() {}

    public ShortenRequest(String url, String idempotencyKey) {
        this.url = url;
        this.idempotencyKey = idempotencyKey;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
