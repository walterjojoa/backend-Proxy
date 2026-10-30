package com.taller.proxy.service;

import com.taller.proxy.dto.DailyUsageResponse;
import com.taller.proxy.dto.QuotaStatusResponse;
import com.taller.proxy.exception.InvalidUpgradeException;
import com.taller.proxy.model.DailyUsage;
import com.taller.proxy.model.Plan;
import com.taller.proxy.model.UserAccount;
import com.taller.proxy.proxy.RateLimitProxyService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Read and administrative operations over the user quota (status, history, upgrade, resets).
 */
@Service
public class QuotaService {

    private static final int HISTORY_DAYS = 7;

    private final UserAccountRepository repository;
    private final Clock clock;

    public QuotaService(UserAccountRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public QuotaStatusResponse getStatus(String userId) {
        UserAccount account = repository.getOrCreate(userId);
        Plan plan = account.getPlan();

        return new QuotaStatusResponse(
                account.getUserId(),
                plan,
                account.getTokensUsedThisMonth(),
                plan.hasUnlimitedTokens() ? null : account.getRemainingTokens(),
                plan.hasUnlimitedTokens() ? null : plan.getMonthlyTokens(),
                nextMonthlyResetDate(),
                account.getRequestsThisMinute(),
                plan.hasUnlimitedRequests() ? null : plan.getRequestsPerMinute(),
                RateLimitProxyService.secondsUntilNextWindow(clock));
    }

    /**
     * Usage of the last 7 days (today included), oldest first. Days without usage return zero.
     */
    public List<DailyUsageResponse> getHistory(String userId) {
        UserAccount account = repository.getOrCreate(userId);
        LocalDate today = LocalDate.now(clock);

        List<DailyUsageResponse> history = new ArrayList<>();
        for (int i = HISTORY_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            DailyUsage usage = account.getUsageOf(day);
            history.add(usage == null
                    ? new DailyUsageResponse(day, 0, 0)
                    : new DailyUsageResponse(day, usage.getTokens(), usage.getRequests()));
        }
        return history;
    }

    public QuotaStatusResponse upgrade(String userId, Plan targetPlan) {
        UserAccount account = repository.getOrCreate(userId);
        Plan newPlan = targetPlan == null ? Plan.PRO : targetPlan;

        if (newPlan.ordinal() <= account.getPlan().ordinal()) {
            throw new InvalidUpgradeException(
                    "Cannot change plan from " + account.getPlan() + " to " + newPlan + ": only upgrades are allowed");
        }
        account.changePlan(newPlan);
        return getStatus(userId);
    }

    public void resetAllMinuteWindows() {
        repository.findAll().forEach(UserAccount::resetMinuteWindow);
    }

    public void resetAllMonthlyQuotas() {
        repository.findAll().forEach(UserAccount::resetMonthlyQuota);
    }

    private LocalDate nextMonthlyResetDate() {
        return LocalDate.now(clock).withDayOfMonth(1).plusMonths(1);
    }
}
