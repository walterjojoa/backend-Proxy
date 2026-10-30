package com.taller.proxy.model;

/**
 * Subscription plans. A negative limit means "unlimited".
 */
public enum Plan {
    FREE(10, 50_000),
    PRO(60, 500_000),
    ENTERPRISE(-1, -1);

    private final int requestsPerMinute;
    private final long monthlyTokens;

    Plan(int requestsPerMinute, long monthlyTokens) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokens = monthlyTokens;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokens() {
        return monthlyTokens;
    }

    public boolean hasUnlimitedRequests() {
        return requestsPerMinute < 0;
    }

    public boolean hasUnlimitedTokens() {
        return monthlyTokens < 0;
    }
}
