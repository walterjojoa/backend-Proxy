package com.taller.proxy.proxy;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;
import com.taller.proxy.exception.RateLimitExceededException;
import com.taller.proxy.model.UserAccount;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.UserAccountRepository;

import java.time.Clock;
import java.time.LocalTime;

/**
 * First proxy of the chain: checks that the user does not exceed the
 * requests per minute allowed by the plan. If the limit is reached the
 * request never reaches the next element of the chain (HTTP 429).
 */
public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserAccountRepository repository;
    private final Clock clock;

    public RateLimitProxyService(AIGenerationService next, UserAccountRepository repository, Clock clock) {
        this.next = next;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = repository.getOrCreate(request.userId());

        if (!account.tryRegisterRequest()) {
            throw new RateLimitExceededException(account.getPlan(), secondsUntilNextWindow(clock));
        }
        return next.generate(request);
    }

    /**
     * The window is the current clock minute; the scheduler clears the counters at second 0.
     */
    public static long secondsUntilNextWindow(Clock clock) {
        return Math.max(1, 60 - LocalTime.now(clock).getSecond());
    }
}
