package com.stockpulse.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of LLMGateway that returns empty responses.
 * Allows the application to start even when no real LLM provider is configured.
 */
@Component
public class NoOpLLMGateway implements LLMGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(NoOpLLMGateway.class);
    
    @Override
    public LLMResponse callLLM(String prompt) {
        logger.warn("NoOp LLMGateway called with prompt: {}", prompt);
        return new LLMResponse("AI not configured", "NOOP", "default");
    }
}