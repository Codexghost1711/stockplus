package com.stockpulse.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for LLM Gateway beans.
 * Provides a no-op implementation as fallback when no real LLM provider is configured.
 */
@Configuration
public class LLMGatewayConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMGatewayConfiguration.class);
    
    @Value("${stockpulse.ai.provider:UNKNOWN}")
    private String provider;
    
    /**
     * Primary LLMGateway bean that provides the appropriate implementation
     * based on configuration.
     * 
     * @param noopLLMGateway the no-op implementation
     * @param liteLLMGateway the LiteLLM implementation
     * @return LLMGateway implementation
     */
    @Bean
    @Primary
    public LLMGateway primaryLLMGateway(
            NoOpLLMGateway noopLLMGateway, 
            LiteLLMGateway liteLLMGateway) {
        
        if ("UNKNOWN".equals(provider) || provider == null || provider.isEmpty()) {
            logger.info("No LLM provider configured, using NoOp implementation");
            return noopLLMGateway;
        }
        
        if ("LITE".equalsIgnoreCase(provider) || "LITELLM".equalsIgnoreCase(provider)) {
            logger.info("LiteLLM provider configured, using LiteLLM implementation");
            return liteLLMGateway;
        }
        
        logger.info("LLM provider configured: {}, but no specific implementation found, using NoOp as fallback", provider);
        return noopLLMGateway;
    }
}