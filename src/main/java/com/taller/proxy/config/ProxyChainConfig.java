package com.taller.proxy.config;

import com.taller.proxy.proxy.QuotaProxyService;
import com.taller.proxy.proxy.RateLimitProxyService;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.MockAIGenerationService;
import com.taller.proxy.service.TokenEstimator;
import com.taller.proxy.service.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Builds the proxy chain:
 * <pre>
 * Controller -> RateLimitProxyService -> QuotaProxyService -> MockAIGenerationService
 * </pre>
 * Every element implements {@link AIGenerationService}, so the controller does not know
 * whether it is talking to a proxy or to the real service.
 */
@Configuration
public class ProxyChainConfig {

    @Bean
    public Clock clock(@Value("${app.timezone}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }

    @Bean
    public MockAIGenerationService mockAIGenerationService(TokenEstimator tokenEstimator,
                                                           @Value("${app.ai.mock-delay-ms}") long delayMs) {
        return new MockAIGenerationService(tokenEstimator, delayMs);
    }

    @Bean
    public QuotaProxyService quotaProxyService(MockAIGenerationService realService,
                                               UserAccountRepository repository,
                                               TokenEstimator tokenEstimator,
                                               Clock clock) {
        return new QuotaProxyService(realService, repository, tokenEstimator, clock);
    }

    @Bean
    @Primary
    public AIGenerationService aiGenerationService(QuotaProxyService quotaProxy,
                                                   UserAccountRepository repository,
                                                   Clock clock) {
        return new RateLimitProxyService(quotaProxy, repository, clock);
    }
}
