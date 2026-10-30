package com.taller.proxy.controller;

/**
 * The user is identified by the X-User-Id header (there is no login in this project).
 */
final class UserIdHeader {

    static final String NAME = "X-User-Id";
    private static final int MAX_LENGTH = 50;

    private UserIdHeader() {
    }

    static String normalize(String userId) {
        String value = userId == null ? "" : userId.trim().toLowerCase();
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Header " + NAME + " must have between 1 and " + MAX_LENGTH + " characters");
        }
        return value;
    }
}
