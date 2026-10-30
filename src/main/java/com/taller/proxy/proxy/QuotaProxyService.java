package com.taller.proxy.proxy;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;
import com.taller.proxy.exception.QuotaExceededException;
import com.taller.proxy.model.UserAccount;
import com.taller.proxy.service.AIGenerationService;
import com.taller.proxy.service.TokenEstimator;
import com.taller.proxy.service.UserAccountRepository;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Second proxy of the chain: checks that the user still has monthly tokens,
 * calls the next element and then discounts the tokens consumed by the request.
 * If the quota is exhausted the request is rejected (HTTP 402).
 */
public class QuotaProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserAccountRepository repository;
    private final TokenEstimator tokenEstimator;
    private final Clock clock;

    public QuotaProxyService(AIGenerationService next, UserAccountRepository repository,
                             TokenEstimator tokenEstimator, Clock clock) {
        this.next = next;
        this.repository = repository;
        this.tokenEstimator = tokenEstimator;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = repository.getOrCreate(request.userId());
        long promptTokens = tokenEstimator.estimate(request.prompt());

        if (!account.hasTokensFor(promptTokens)) {
            throw new QuotaExceededException(
                    account.getPlan(), account.getTokensUsedThisMonth(), account.getPlan().getMonthlyTokens());
        }

        GenerationResponse response = next.generate(request);
        account.consumeTokens(response.totalTokens(), LocalDate.now(clock));
        return response;
    }
}
