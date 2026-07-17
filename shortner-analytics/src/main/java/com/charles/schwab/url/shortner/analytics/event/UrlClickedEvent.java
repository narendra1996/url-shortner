package com.charles.schwab.url.shortner.analytics.event;

public class UrlClickedEvent {
    private String shortCode;
    private String userAgent;
    private String ipHash;

    public UrlClickedEvent(String shortCode, String userAgent, String ipHash) {
        this.shortCode = shortCode;
        this.userAgent = userAgent;
        this.ipHash = ipHash;
    }

    public String getShortCode() { return shortCode; }
    public String getUserAgent() { return userAgent; }
    public String getIpHash() { return ipHash; }
}
