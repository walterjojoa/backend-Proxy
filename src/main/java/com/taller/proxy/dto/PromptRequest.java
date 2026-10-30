package com.taller.proxy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/ai/generate. The user comes in the X-User-Id header.
 */
public record PromptRequest(
        @NotBlank(message = "prompt must not be empty")
        @Size(max = 4000, message = "prompt must have at most 4000 characters")
        String prompt) {
}
