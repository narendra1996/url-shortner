package com.charles.schwab.url.shortner.common.dto;

public class ClickStatsResponse {
    private String shortCode;
    private long totalClicks;

    public ClickStatsResponse() {}

    public ClickStatsResponse(String shortCode, long totalClicks) {
        this.shortCode = shortCode;
        this.totalClicks = totalClicks;
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }
}
