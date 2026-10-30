package com.taller.proxy.service;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Real subject of the Proxy pattern: simulates an expensive AI text generation.
 * It knows nothing about plans, rate limits or quotas.
 */
public class MockAIGenerationService implements AIGenerationService {

    private static final List<String> PREDEFINED_ANSWERS = List.of(
            "The Proxy pattern provides a surrogate or placeholder for another object to control access to it.",
            "Rate limiting protects a service from abuse by restricting how many requests a client can make in a time window.",
            "A quota defines how many resources a user can consume in a billing period, such as tokens per month.",
            "Lazy loading delays the creation of an expensive object until the moment it is really needed.",
            "Spring Boot makes it easy to create stand-alone, production-grade applications that you can just run.",
            "Caching stores the result of expensive operations so the next identical request can be answered faster.",
            "Large language models split text into tokens; one token is roughly four characters of English text.",
            "Design patterns are reusable solutions to common problems in software design.",
            "A good API returns meaningful HTTP status codes, such as 429 for too many requests and 402 for payment required.",
            "Chaining proxies lets you add responsibilities step by step without modifying the real service."
    );

    private final TokenEstimator tokenEstimator;
    private final long delayMs;

    public MockAIGenerationService(TokenEstimator tokenEstimator, long delayMs) {
        this.tokenEstimator = tokenEstimator;
        this.delayMs = delayMs;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        simulateLatency();

        String text = PREDEFINED_ANSWERS.get(ThreadLocalRandom.current().nextInt(PREDEFINED_ANSWERS.size()));
        long promptTokens = tokenEstimator.estimate(request.prompt());
        long completionTokens = tokenEstimator.estimate(text);

        return new GenerationResponse(
                text,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                System.currentTimeMillis() - start,
                Instant.now());
    }

    private void simulateLatency() {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
