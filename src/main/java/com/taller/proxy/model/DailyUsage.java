package com.taller.proxy.model;

import java.time.LocalDate;

/**
 * Tokens and requests consumed by a user on a single day.
 */
public class DailyUsage {

    private final LocalDate date;
    private long tokens;
    private int requests;

    public DailyUsage(LocalDate date) {
        this.date = date;
    }

    public void add(long tokens) {
        this.tokens += tokens;
        this.requests++;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTokens() {
        return tokens;
    }

    public int getRequests() {
        return requests;
    }
}
