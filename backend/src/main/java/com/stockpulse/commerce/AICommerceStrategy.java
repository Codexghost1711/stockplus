package com.stockpulse.commerce;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.ai.LLMGateway;
import com.stockpulse.ai.LLMResponse;
import com.stockpulse.suggestion.PricingSuggestion;

@Component("AI")
public class AICommerceStrategy implements CommerceStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(AICommerceStrategy.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final LLMGateway llmGateway;
    private final RuleBasedCommerceStrategy ruleBasedCommerceStrategy;

    public AICommerceStrategy(LLMGateway llmGateway, RuleBasedCommerceStrategy ruleBasedCommerceStrategy) {
        this.llmGateway = llmGateway;
        this.ruleBasedCommerceStrategy = ruleBasedCommerceStrategy;
    }

    @Override
    public CommerceRecommendation generateRecommendation(RecommendationContext context) {
        CommerceRecommendation fallback = ruleBasedCommerceStrategy.generateRecommendation(context);
        try {
            logger.debug("Generating AI-powered recommendation for product: {}", context.getSku());
            
            // Build prompts for pricing and reorder recommendations
            String pricingPrompt = buildPricingPrompt(context);
            String reorderPrompt = buildReorderPrompt(context);
            
            // Call LLM for pricing recommendation
            LLMResponse pricingResponse = llmGateway.callLLM(pricingPrompt);
            logger.debug("Received pricing LLM response from provider: {}", pricingResponse.getProvider());
            
            // Call LLM for reorder recommendation
            LLMResponse reorderResponse = llmGateway.callLLM(reorderPrompt);
            logger.debug("Received reorder LLM response from provider: {}", reorderResponse.getProvider());
            
            // Parse responses and create recommendation
            CommerceRecommendation recommendation = new CommerceRecommendation();
            
            // Process pricing response
            CommerceRecommendation.PricingRecommendation pricingRec = parsePricingResponse(
                    pricingResponse.getRawResponse(), context, fallback.getPricingRecommendation());
            recommendation.setPricingRecommendation(pricingRec);
            
            // Process reorder response
            CommerceRecommendation.ReorderRecommendation reorderRec = parseReorderResponse(
                    reorderResponse.getRawResponse(), fallback.getReorderRecommendation());
            recommendation.setReorderRecommendation(reorderRec);
            
            return recommendation;
        } catch (Exception e) {
            logger.warn("Failed to generate AI recommendation, falling back to rule-based recommendation: {}", e.getMessage());
            return markFallback(fallback, "AI request failed; using rule-based recommendations.");
        }
    }
    
    private String buildPricingPrompt(RecommendationContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI commerce advisor helping with dynamic pricing decisions.\n");
        prompt.append("Product: ").append(context.getName()).append(" (SKU: ").append(context.getSku()).append(")\n");
        prompt.append("Category: ").append(context.getCategory()).append("\n");
        prompt.append("Current Price: $").append(context.getCurrentPrice()).append("\n");
        prompt.append("Stock Level: ").append(context.getStockLevel()).append("\n");
        prompt.append("Reorder Threshold: ").append(context.getReorderThreshold()).append("\n");
        prompt.append("Demand Velocity: ").append(context.getDemandVelocity()).append("\n");
        prompt.append("Trigger Reason: ").append(context.getTriggerReason()).append("\n\n");
        
        RecommendationContext.TriggerReason triggerReason = Optional.ofNullable(context.getTriggerReason())
                .orElse(RecommendationContext.TriggerReason.INITIAL);
        switch (triggerReason) {
            case INVENTORY_LOW -> {
                prompt.append("TRIGGER: Inventory is low. The stock level is below the reorder threshold.\n");
                prompt.append("TASK: Recommend a pricing strategy considering this low inventory situation.\n");
                prompt.append("OPTIONS:\n");
                prompt.append("1. Increase price to maximize revenue from remaining inventory\n");
                prompt.append("2. Hold price to maintain current demand\n");
                prompt.append("3. Decrease price to clear remaining inventory quickly\n");
                prompt.append("DECISION FACTORS:\n");
                prompt.append("- Category demand patterns\n");
                prompt.append("- Remaining stock quantity\n");
                prompt.append("- Current price position vs competitors (if known)\n\n");
            }
            case DEMAND_SPIKE -> {
                prompt.append("TRIGGER: Demand has spiked significantly above normal velocity.\n");
                prompt.append("TASK: Recommend a pricing strategy for this high-demand period.\n");
                prompt.append("OPTIONS:\n");
                prompt.append("1. Increase price to maximize revenue during peak demand\n");
                prompt.append("2. Hold price to maintain current momentum\n");
                prompt.append("3. Decrease price to attract even more customers during the trend\n");
                prompt.append("DECISION FACTORS:\n");
                prompt.append("- Current stock availability\n");
                prompt.append("- Historical demand patterns for this category\n");
                prompt.append("- Sustainability of the demand spike\n\n");
            }
            default -> {
                prompt.append("TASK: Recommend an optimal pricing strategy for this product.\n");
                prompt.append("DECISION FACTORS:\n");
                prompt.append("- Current stock level vs reorder threshold\n");
                prompt.append("- Demand velocity vs category average\n");
                prompt.append("- Price sensitivity for this product category\n\n");
            }
        }
        
        prompt.append("Return ONLY a valid JSON object, with no Markdown fences or surrounding text.\n");
        prompt.append("RETURN FORMAT (JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"recommendedPrice\": 29.99,\n");
        prompt.append("  \"direction\": \"INCREASE\",\n");
        prompt.append("  \"confidence\": 0.85,\n");
        prompt.append("  \"reasoning\": \"Detailed explanation of the recommendation\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    private String buildReorderPrompt(RecommendationContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI commerce advisor helping with inventory replenishment decisions.\n");
        prompt.append("Product: ").append(context.getName()).append(" (SKU: ").append(context.getSku()).append(")\n");
        prompt.append("Category: ").append(context.getCategory()).append("\n");
        prompt.append("Current Stock: ").append(context.getStockLevel()).append("\n");
        prompt.append("Reorder Threshold: ").append(context.getReorderThreshold()).append("\n");
        prompt.append("Demand Velocity: ").append(context.getDemandVelocity()).append("\n");
        prompt.append("Trigger Reason: ").append(context.getTriggerReason()).append("\n\n");
        
        prompt.append("Based on this information, please provide:\n");
        prompt.append("1. A recommended reorder quantity (positive integer)\n");
        prompt.append("2. Confidence score (0.0 to 1.0)\n");
        prompt.append("3. Brief reasoning (1-2 sentences)\n\n");
        
        prompt.append("Return ONLY a valid JSON object, with no Markdown fences or surrounding text. ");
        prompt.append("Include these fields: recommendedQuantity, confidence, reasoning.");
        
        return prompt.toString();
    }
    
    private CommerceRecommendation.PricingRecommendation parsePricingResponse(
            String response,
            RecommendationContext context,
            CommerceRecommendation.PricingRecommendation ruleFallback) {
        CommerceRecommendation.PricingRecommendation pricingRec = new CommerceRecommendation.PricingRecommendation();
        try {
            JsonNode json = parseJsonObject(response);
            JsonNode priceNode = json.get("recommendedPrice");
            if (priceNode == null || !priceNode.isNumber() || !Double.isFinite(priceNode.asDouble())
                    || priceNode.asDouble() < 0) {
                throw new IllegalArgumentException("Missing or invalid recommendedPrice");
            }

            double recommendedPrice = priceNode.asDouble();
            JsonNode directionNode = json.get("direction");
            PricingSuggestion.PricingDirection direction = directionNode == null || directionNode.isNull()
                    ? inferDirection(recommendedPrice, context.getCurrentPrice())
                    : PricingSuggestion.PricingDirection.valueOf(directionNode.asText().trim().toUpperCase());

            pricingRec.setRecommendedPrice(recommendedPrice);
            pricingRec.setDirection(direction);
            pricingRec.setConfidence(readConfidence(json));
            pricingRec.setReasoning(readReasoning(json, "AI-generated pricing recommendation."));
        } catch (IOException | IllegalArgumentException e) {
            logger.warn("Failed to parse pricing response: {}", e.getMessage());
            ruleFallback.setReasoning("AI pricing response was invalid; using rule-based fallback. "
                    + ruleFallback.getReasoning());
            return ruleFallback;
        }
        return pricingRec;
    }
    
    private CommerceRecommendation.ReorderRecommendation parseReorderResponse(
            String response, CommerceRecommendation.ReorderRecommendation ruleFallback) {
        CommerceRecommendation.ReorderRecommendation reorderRec = new CommerceRecommendation.ReorderRecommendation();
        try {
            JsonNode json = parseJsonObject(response);
            JsonNode quantityNode = json.get("recommendedQuantity");
            if (quantityNode == null || !quantityNode.isIntegralNumber() || !quantityNode.canConvertToInt()
                    || quantityNode.asInt() < 1) {
                throw new IllegalArgumentException("Missing or invalid recommendedQuantity");
            }

            reorderRec.setRecommendedQuantity(quantityNode.asInt());
            reorderRec.setConfidence(readConfidence(json));
            reorderRec.setReasoning(readReasoning(json, "AI-generated reorder recommendation."));
        } catch (IOException | IllegalArgumentException e) {
            logger.warn("Failed to parse reorder response: {}", e.getMessage());
            ruleFallback.setReasoning("AI reorder response was invalid; using rule-based fallback. "
                    + ruleFallback.getReasoning());
            return ruleFallback;
        }
        return reorderRec;
    }

    private JsonNode parseJsonObject(String response) throws IOException {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("AI response is empty");
        }

        String jsonText = response.trim();
        if (jsonText.startsWith("```")) {
            int contentStart = jsonText.indexOf('\n');
            int closingFence = jsonText.lastIndexOf("```");
            if (contentStart < 0 || closingFence <= contentStart) {
                throw new IllegalArgumentException("AI response has an invalid Markdown code fence");
            }
            jsonText = jsonText.substring(contentStart + 1, closingFence).trim();
        }

        JsonNode json = objectMapper.readTree(jsonText);
        if (json == null || !json.isObject()) {
            throw new IllegalArgumentException("AI response must be a JSON object");
        }
        return json;
    }

    private double readConfidence(JsonNode json) {
        JsonNode confidenceNode = json.get("confidence");
        if (confidenceNode == null || !confidenceNode.isNumber()) {
            throw new IllegalArgumentException("Missing or invalid confidence");
        }
        double confidence = confidenceNode.asDouble();
        if (!Double.isFinite(confidence) || confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        return confidence;
    }

    private String readReasoning(JsonNode json, String fallback) {
        JsonNode reasoningNode = json.get("reasoning");
        return reasoningNode != null && reasoningNode.isTextual() && !reasoningNode.asText().isBlank()
                ? reasoningNode.asText()
                : fallback;
    }

    private PricingSuggestion.PricingDirection inferDirection(double recommendedPrice, Double currentPrice) {
        if (currentPrice == null || Double.compare(recommendedPrice, currentPrice) == 0) {
            return PricingSuggestion.PricingDirection.HOLD;
        }
        return recommendedPrice > currentPrice
                ? PricingSuggestion.PricingDirection.INCREASE
                : PricingSuggestion.PricingDirection.DECREASE;
    }

    private CommerceRecommendation markFallback(CommerceRecommendation recommendation, String reason) {
        recommendation.getPricingRecommendation().setReasoning(
                reason + " " + recommendation.getPricingRecommendation().getReasoning());
        recommendation.getReorderRecommendation().setReasoning(
                reason + " " + recommendation.getReorderRecommendation().getReasoning());
        return recommendation;
    }
}
