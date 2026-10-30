package com.taller.proxy.dto;

import com.taller.proxy.model.Plan;

import java.time.LocalDate;

/**
 * Current quota and rate-limit state of a user.
 * Limits and remaining values are null when the plan is unlimited.
 */
public record QuotaStatusResponse(
        String userId,
        Plan plan,
        long tokensUsed,
        Long tokensRemaining,
        Long tokenLimit,
        LocalDate resetDate,
        int requestsThisMinute,
        Integer requestsPerMinuteLimit,
        long secondsUntilRateLimitReset) {
}
