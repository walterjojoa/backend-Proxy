package com.taller.proxy.proxy;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;
import com.taller.proxy.exception.QuotaExceededException;
import com.taller.proxy.exception.RateLimitExceededException;
import com.taller.proxy.model.Plan;
import com.taller.proxy.model.UserAccount;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.MockAIGenerationService;
import com.taller.proxy.service.TokenEstimator;
import com.taller.proxy.service.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProxyChainTest {

    // 2026-10-30 10:15:20 in Bogota -> 40 seconds until the next minute
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-30T15:15:20Z"), ZoneId.of("America/Bogota"));
    private final TokenEstimator tokenEstimator = new TokenEstimator();

    private UserAccountRepository repository;
    private AtomicInteger realServiceCalls;
    private AIGenerationService chain;

    @BeforeEach
    void setUp() {
        repository = new UserAccountRepository();
        realServiceCalls = new AtomicInteger();

        MockAIGenerationService mock = new MockAIGenerationService(tokenEstimator, 0);
        AIGenerationService countingRealService = request -> {
            realServiceCalls.incrementAndGet();
            return mock.generate(request);
        };
        chain = new RateLimitProxyService(
                new QuotaProxyService(countingRealService, repository, tokenEstimator, clock),
                repository, clock);
    }

    @Test
    void freePlanAllowsTenRequestsPerMinuteAndBlocksTheEleventh() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("ana", "hello"));
        }

        assertThatThrownBy(() -> chain.generate(new GenerationRequest("ana", "hello")))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> assertThat(((RateLimitExceededException) ex).getRetryAfterSeconds()).isEqualTo(40));
        assertThat(realServiceCalls.get()).isEqualTo(10);
    }

    @Test
    void rateLimitIsPerUser() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("ana", "hello"));
        }

        GenerationResponse response = chain.generate(new GenerationRequest("bob", "hello"));

        assertThat(response.text()).isNotBlank();
    }

    @Test
    void resettingTheWindowAllowsNewRequests() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("ana", "hello"));
        }
        repository.findAll().forEach(UserAccount::resetMinuteWindow);

        assertThat(chain.generate(new GenerationRequest("ana", "hello")).text()).isNotBlank();
    }

    @Test
    void quotaProxyDiscountsTheTokensOfEachRequest() {
        GenerationResponse response = chain.generate(new GenerationRequest("ana", "a".repeat(400)));

        UserAccount account = repository.getOrCreate("ana");
        assertThat(response.promptTokens()).isEqualTo(100);
        assertThat(account.getTokensUsedThisMonth()).isEqualTo(response.totalTokens());
        assertThat(account.getUsageOf(LocalDate.now(clock)).getRequests()).isEqualTo(1);
    }

    @Test
    void exhaustedQuotaIsRejectedWithoutCallingTheRealService() {
        // 200,004 characters = 50,001 tokens, more than the 50,000 of the FREE plan
        String hugePrompt = "a".repeat(200_004);

        assertThatThrownBy(() -> chain.generate(new GenerationRequest("ana", hugePrompt)))
                .isInstanceOf(QuotaExceededException.class);
        assertThat(realServiceCalls.get()).isZero();
    }

    @Test
    void enterprisePlanHasNoLimits() {
        repository.getOrCreate("corp").changePlan(Plan.ENTERPRISE);

        for (int i = 0; i < 100; i++) {
            chain.generate(new GenerationRequest("corp", "hello"));
        }
        chain.generate(new GenerationRequest("corp", "a".repeat(800_000)));

        assertThat(realServiceCalls.get()).isEqualTo(101);
    }
}
