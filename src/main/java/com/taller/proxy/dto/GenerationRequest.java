package com.taller.proxy.dto;

/**
 * Request that travels through the proxy chain down to the real AI service.
 */
public record GenerationRequest(String userId, String prompt) {
}
