package com.stockpulse.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "stockpulse.ai.provider=LITE",
    "stockpulse.ai.api-key=test-key",
    "spring.datasource.url=jdbc:h2:mem:stockpulse-lite-llm-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class LiteLLMGatewayTest {

    @Autowired
    private LLMGateway llmGateway;
    
    @Test
    void contextLoadsAndLlmGatewayBeanExists() {
        // The application context should load successfully
        // and the LLMGateway bean should be available
        assertNotNull(llmGateway, "LLMGateway bean should be available");
    }
    
    @Test
    void liteLLMGatewayShouldBeInjected() {
        // Verify that the LiteLLMGateway is injected when provider is set to LITE
        assertTrue(llmGateway instanceof LiteLLMGateway, 
            "When provider is LITE, LiteLLMGateway should be injected");
    }
}