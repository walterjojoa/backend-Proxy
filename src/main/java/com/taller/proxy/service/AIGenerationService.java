package com.taller.proxy.service;

import com.taller.proxy.dto.GenerationRequest;
import com.taller.proxy.dto.GenerationResponse;

/**
 * Subject of the Proxy pattern: common interface shared by the real AI service
 * and every proxy that wraps it. The client only knows this interface.
 */
public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);
}
