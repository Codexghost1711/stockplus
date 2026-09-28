package com.stockpulse.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "stockpulse.ai.provider=UNKNOWN",
    "spring.datasource.url=jdbc:h2:mem:stockpulse-llm-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class LLMGatewayTest {

    @Autowired(required = false)
    private LLMGateway llmGateway;
    
    @Test
    void contextLoadsAndLlmGatewayBeanExists() {
        // The application context should load successfully
        // and the LLMGateway bean should be available
        assertNotNull(llmGateway, "LLMGateway bean should be available");
    }
    
    @Test
    void noopLLMGatewayShouldReturnResponse() {
        // If LLMGateway is available, it should return a response
        if (llmGateway != null) {
            LLMResponse response = llmGateway.callLLM("Test prompt");
            assertNotNull(response, "LLMResponse should not be null");
            assertNotNull(response.getRawResponse(), "Raw response should not be null");
            assertNotNull(response.getProvider(), "Provider should not be null");
            assertNotNull(response.getModel(), "Model should not be null");
        }
    }
    
    @Test
    void noopLLMGatewayShouldBeInjectedByDefault() {
        // By default, when no provider is configured, NoOpLLMGateway should be injected
        assertTrue(llmGateway instanceof NoOpLLMGateway, 
            "When no provider is configured, NoOpLLMGateway should be injected");
    }
}
