package com.taller.proxy.exception;

import com.taller.proxy.model.Plan;

public class RateLimitExceededException extends RuntimeException {

    private final Plan plan;
    private final long retryAfterSeconds;

    public RateLimitExceededException(Plan plan, long retryAfterSeconds) {
        super("Rate limit of " + plan.getRequestsPerMinute() + " requests per minute exceeded for plan " + plan
                + ". Try again in " + retryAfterSeconds + " seconds");
        this.plan = plan;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Plan getPlan() {
        return plan;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
