package com.taller.proxy.scheduler;

import com.taller.proxy.service.QuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UsageResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(UsageResetScheduler.class);

    private final QuotaService quotaService;

    public UsageResetScheduler(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    /**
     * Every minute, at second 0: clears the requests-per-minute counters.
     */
    @Scheduled(cron = "0 * * * * *", zone = "${app.timezone}")
    public void resetRateLimits() {
        quotaService.resetAllMinuteWindows();
    }

    /**
     * First day of every month at 00:00: restores the monthly token quota.
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.timezone}")
    public void resetMonthlyQuotas() {
        quotaService.resetAllMonthlyQuotas();
        log.info("Monthly token quotas have been reset");
    }
}
