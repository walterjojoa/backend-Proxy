package com.taller.proxy.exception;

import com.taller.proxy.model.Plan;

public class QuotaExceededException extends RuntimeException {

    private final Plan plan;

    public QuotaExceededException(Plan plan, long tokensUsed, long tokenLimit) {
        super("Monthly token quota exhausted for plan " + plan + " (" + tokensUsed + "/" + tokenLimit
                + " tokens). Upgrade your plan to keep using the service");
        this.plan = plan;
    }

    public Plan getPlan() {
        return plan;
    }
}
