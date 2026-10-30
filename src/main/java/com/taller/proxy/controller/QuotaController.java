package com.taller.proxy.controller;

import com.taller.proxy.dto.DailyUsageResponse;
import com.taller.proxy.dto.QuotaStatusResponse;
import com.taller.proxy.dto.UpgradeRequest;
import com.taller.proxy.service.QuotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping("/status")
    public QuotaStatusResponse status(@RequestHeader(UserIdHeader.NAME) String userId) {
        return quotaService.getStatus(UserIdHeader.normalize(userId));
    }

    @GetMapping("/history")
    public List<DailyUsageResponse> history(@RequestHeader(UserIdHeader.NAME) String userId) {
        return quotaService.getHistory(UserIdHeader.normalize(userId));
    }

    @PostMapping("/upgrade")
    public QuotaStatusResponse upgrade(@RequestHeader(UserIdHeader.NAME) String userId,
                                       @RequestBody(required = false) UpgradeRequest body) {
        return quotaService.upgrade(UserIdHeader.normalize(userId), body == null ? null : body.plan());
    }
}
