package com.stockpulse.commerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockpulse.ai.LLMGateway;
import com.stockpulse.ai.LLMResponse;
import com.stockpulse.suggestion.PricingSuggestion;

@ExtendWith(MockitoExtension.class)
class AICommerceStrategyTest {

    @Mock
    private LLMGateway llmGateway;

    private AICommerceStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AICommerceStrategy(llmGateway, new RuleBasedCommerceStrategy());
    }

    @Test
    void generateRecommendation_ParsesJsonAndMarkdownFencedResponses() {
        when(llmGateway.callLLM(anyString()))
                .thenReturn(response("```json\n{\"recommendedPrice\":24.5,\"direction\":\"INCREASE\",\"confidence\":0.85,\"reasoning\":\"Demand is rising\"}\n```"))
                .thenReturn(response("{\"recommendedQuantity\":12,\"confidence\":0.8,\"reasoning\":\"Below target stock\"}"));

        CommerceRecommendation recommendation = strategy.generateRecommendation(context());

        assertEquals(24.5, recommendation.getPricingRecommendation().getRecommendedPrice());
        assertEquals(PricingSuggestion.PricingDirection.INCREASE,
                recommendation.getPricingRecommendation().getDirection());
        assertEquals(0.85, recommendation.getPricingRecommendation().getConfidence());
        assertEquals(12, recommendation.getReorderRecommendation().getRecommendedQuantity());
        assertEquals("Below target stock", recommendation.getReorderRecommendation().getReasoning());
    }

    @Test
    void generateRecommendation_InvalidPricingResponseFallsBackWithoutDiscardingValidReorder() {
        when(llmGateway.callLLM(anyString()))
                .thenReturn(response("not valid JSON"))
                .thenReturn(response("{\"recommendedQuantity\":9,\"confidence\":0.75,\"reasoning\":\"Valid reorder\"}"));

        CommerceRecommendation recommendation = strategy.generateRecommendation(context());

        assertEquals(22.0, recommendation.getPricingRecommendation().getRecommendedPrice());
        assertEquals(PricingSuggestion.PricingDirection.INCREASE,
                recommendation.getPricingRecommendation().getDirection());
        assertTrue(recommendation.getPricingRecommendation().getReasoning().contains("rule-based fallback"));
        assertEquals(9, recommendation.getReorderRecommendation().getRecommendedQuantity());
    }

    @Test
    void generateRecommendation_InvalidReorderResponseFallsBackWithoutDiscardingValidPricing() {
        when(llmGateway.callLLM(anyString()))
                .thenReturn(response("{\"recommendedPrice\":22.0,\"confidence\":0.9,\"reasoning\":\"Valid price\"}"))
                .thenReturn(response("{\"recommendedQuantity\":0,\"confidence\":0.7}"));

        CommerceRecommendation recommendation = strategy.generateRecommendation(context());

        assertEquals(22.0, recommendation.getPricingRecommendation().getRecommendedPrice());
        assertEquals(PricingSuggestion.PricingDirection.INCREASE,
                recommendation.getPricingRecommendation().getDirection());
        assertEquals(11, recommendation.getReorderRecommendation().getRecommendedQuantity());
        assertTrue(recommendation.getReorderRecommendation().getReasoning().contains("rule-based fallback"));
        }

        @Test
        void generateRecommendation_WhenGatewayFails_UsesRuleBasedRecommendations() {
        when(llmGateway.callLLM(anyString())).thenThrow(new IllegalStateException("provider unavailable"));

        CommerceRecommendation recommendation = strategy.generateRecommendation(context());

        assertEquals(22.0, recommendation.getPricingRecommendation().getRecommendedPrice());
        assertEquals(PricingSuggestion.PricingDirection.INCREASE,
            recommendation.getPricingRecommendation().getDirection());
        assertEquals(11, recommendation.getReorderRecommendation().getRecommendedQuantity());
        assertTrue(recommendation.getPricingRecommendation().getReasoning().contains("AI request failed"));
    }

    @Test
    void generateRecommendation_WhenProviderReturnsNoOpText_UsesRuleBasedRecommendations() {
        when(llmGateway.callLLM(anyString())).thenReturn(response("AI not configured"));

        CommerceRecommendation recommendation = strategy.generateRecommendation(context());

        assertEquals(22.0, recommendation.getPricingRecommendation().getRecommendedPrice());
        assertEquals(11, recommendation.getReorderRecommendation().getRecommendedQuantity());
        assertTrue(recommendation.getPricingRecommendation().getReasoning().contains("rule-based fallback"));
        assertTrue(recommendation.getReorderRecommendation().getReasoning().contains("rule-based fallback"));
    }

    private RecommendationContext context() {
        RecommendationContext context = new RecommendationContext();
        context.setCurrentPrice(20.0);
        context.setStockLevel(4);
        context.setReorderThreshold(5);
        context.setTriggerReason(RecommendationContext.TriggerReason.INVENTORY_LOW);
        return context;
    }

    private LLMResponse response(String rawResponse) {
        return new LLMResponse(rawResponse, "TEST", "test-model");
    }
}