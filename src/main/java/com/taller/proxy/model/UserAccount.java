package com.taller.proxy.model;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

/**
 * In-memory state of a user: plan, rate-limit counter, monthly quota and daily history.
 * Mutating methods are synchronized because several requests of the same user
 * can arrive at the same time.
 */
public class UserAccount {

    private static final int HISTORY_DAYS_TO_KEEP = 31;

    private final String userId;
    private Plan plan = Plan.FREE;
    private int requestsThisMinute;
    private long tokensUsedThisMonth;
    private final TreeMap<LocalDate, DailyUsage> dailyUsage = new TreeMap<>();

    public UserAccount(String userId) {
        this.userId = userId;
    }

    /**
     * Registers a new request if the per-minute limit of the plan allows it.
     *
     * @return true if the request was accepted, false if the limit was reached
     */
    public synchronized boolean tryRegisterRequest() {
        if (!plan.hasUnlimitedRequests() && requestsThisMinute >= plan.getRequestsPerMinute()) {
            return false;
        }
        requestsThisMinute++;
        return true;
    }

    public synchronized boolean hasTokensFor(long tokens) {
        return plan.hasUnlimitedTokens() || tokensUsedThisMonth + tokens <= plan.getMonthlyTokens();
    }

    public synchronized void consumeTokens(long tokens, LocalDate day) {
        tokensUsedThisMonth += tokens;
        dailyUsage.computeIfAbsent(day, DailyUsage::new).add(tokens);
        while (dailyUsage.size() > HISTORY_DAYS_TO_KEEP) {
            dailyUsage.pollFirstEntry();
        }
    }

    public synchronized void resetMinuteWindow() {
        requestsThisMinute = 0;
    }

    public synchronized void resetMonthlyQuota() {
        tokensUsedThisMonth = 0;
    }

    public synchronized void changePlan(Plan newPlan) {
        this.plan = newPlan;
    }

    /**
     * @return remaining tokens of the month, or -1 if the plan is unlimited
     */
    public synchronized long getRemainingTokens() {
        if (plan.hasUnlimitedTokens()) {
            return -1;
        }
        return Math.max(0, plan.getMonthlyTokens() - tokensUsedThisMonth);
    }

    public synchronized DailyUsage getUsageOf(LocalDate day) {
        return dailyUsage.get(day);
    }

    public synchronized List<DailyUsage> getDailyUsage() {
        return List.copyOf(dailyUsage.values());
    }

    public String getUserId() {
        return userId;
    }

    public synchronized Plan getPlan() {
        return plan;
    }

    public synchronized int getRequestsThisMinute() {
        return requestsThisMinute;
    }

    public synchronized long getTokensUsedThisMonth() {
        return tokensUsedThisMonth;
    }
}
