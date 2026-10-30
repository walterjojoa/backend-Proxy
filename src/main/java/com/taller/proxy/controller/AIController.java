package com.taller.proxy.controller;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;
import com.taller.proxy.dto.PromptRequest;
import com.taller.proxy.service.AIGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    // Injected through the interface: Spring gives the head of the proxy chain (RateLimitProxyService)
    private final AIGenerationService aiGenerationService;

    public AIController(AIGenerationService aiGenerationService) {
        this.aiGenerationService = aiGenerationService;
    }

    @PostMapping("/generate")
    public GenerationResponse generate(@RequestHeader(UserIdHeader.NAME) String userId,
                                       @Valid @RequestBody PromptRequest body) {
        GenerationRequest request = new GenerationRequest(UserIdHeader.normalize(userId), body.prompt());
        return aiGenerationService.generate(request);
    }
}
