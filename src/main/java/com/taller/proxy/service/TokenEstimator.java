package com.taller.proxy.service;

import org.springframework.stereotype.Component;

/**
 * Approximates the number of tokens of a text using the common rule
 * "1 token ~ 4 characters". The frontend uses the same formula for its estimator.
 */
@Component
public class TokenEstimator {

    private static final double CHARS_PER_TOKEN = 4.0;

    public long estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (long) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }
}
