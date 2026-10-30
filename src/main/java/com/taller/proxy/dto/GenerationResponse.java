package com.taller.proxy.dto;

import java.time.Instant;

public record GenerationResponse(
        String text,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        long latencyMs,
        Instant createdAt) {
}
