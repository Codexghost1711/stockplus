package com.stockpulse.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.stockpulse.commerce.CommerceRecommendation;
import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.commerce.RecommendationContext;
import com.stockpulse.product.Product;
import com.stockpulse.product.ProductRepository;
import com.stockpulse.suggestion.PricingSuggestion;
import com.stockpulse.suggestion.ReorderSuggestion;
import com.stockpulse.suggestion.SuggestionService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RecommendationEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationEventHandler.class);
    private static final double DEMAND_SPIKE_THRESHOLD = 15.0;

    private final ProductRepository productRepository;
    private final SuggestionService suggestionService;
    private final CommerceStrategyRegistry commerceStrategyRegistry;
    
    // Track the previous demand velocity for each product to detect transitions
    private final Map<Long, Double> previousDemandVelocities = new ConcurrentHashMap<>();

    public RecommendationEventHandler(
            ProductRepository productRepository,
            SuggestionService suggestionService,
            CommerceStrategyRegistry commerceStrategyRegistry) {
        this.productRepository = productRepository;
        this.suggestionService = suggestionService;
        this.commerceStrategyRegistry = commerceStrategyRegistry;
    }

    @Async
    @EventListener
    public void handleInventoryEvent(InventoryEvent event) {
        Product product = productRepository.findById(event.getProductId()).orElse(null);
        if (product == null) {
            logger.warn("Product not found for inventory event: {}", event.getProductId());
            return;
        }

        Integer stockLevel = event.getStockLevel();
        Integer reorderThreshold = product.getReorderThreshold();
        if (stockLevel != null && reorderThreshold != null && stockLevel < reorderThreshold) {
            if (!suggestionService.hasPendingSuggestions(
                    event.getProductId(), PricingSuggestion.TriggerReason.INVENTORY_LOW)) {
                generateAndSaveRecommendations(product, RecommendationContext.TriggerReason.INVENTORY_LOW);
            }
        }
    }

    @Async
    @EventListener
    public void handleDemandEvent(DemandEvent event) {
        Product product = productRepository.findById(event.getProductId()).orElse(null);
        if (product == null) {
            logger.warn("Product not found for demand event: {}", event.getProductId());
            return;
        }

        Double demandVelocity = event.getDemandVelocity();
        if (demandVelocity == null) {
            return;
        }
        
        // Get the previous demand velocity for this product
        Double previousVelocity = previousDemandVelocities.getOrDefault(event.getProductId(), 0.0);
        
        // Update the stored previous velocity for next time
        previousDemandVelocities.put(event.getProductId(), demandVelocity);
        
        // Check for transition: previous <= threshold AND current > threshold
        if (previousVelocity <= DEMAND_SPIKE_THRESHOLD && demandVelocity > DEMAND_SPIKE_THRESHOLD) {
            if (!suggestionService.hasPendingSuggestions(
                    event.getProductId(), PricingSuggestion.TriggerReason.DEMAND_SPIKE)) {
                generateAndSaveRecommendations(product, RecommendationContext.TriggerReason.DEMAND_SPIKE);
            }
        }
    }

    private void generateAndSaveRecommendations(
            Product product, RecommendationContext.TriggerReason triggerReason) {
        try {
            RecommendationContext context = new RecommendationContext();
            context.setProductId(product.getId());
            context.setSku(product.getSku());
            context.setName(product.getName());
            context.setCategory(product.getCategory() == null ? null : product.getCategory().name());
            context.setCurrentPrice(product.getCurrentPrice());
            context.setStockLevel(product.getStockLevel());
            context.setReorderThreshold(product.getReorderThreshold());
            context.setDemandVelocity(product.getDemandVelocity());
            context.setTriggerReason(triggerReason);

            CommerceRecommendation recommendation = commerceStrategyRegistry
                    .getActiveStrategy().generateRecommendation(context);

            suggestionService.createPricingSuggestion(createPricingSuggestion(product, recommendation, triggerReason));
            suggestionService.createReorderSuggestion(createReorderSuggestion(product, recommendation, triggerReason));
        } catch (Exception exception) {
            logger.error("Failed to generate recommendations for product ID {}: {}",
                    product.getId(), exception.getMessage(), exception);
        }
    }

    private PricingSuggestion createPricingSuggestion(
            Product product,
            CommerceRecommendation recommendation,
            RecommendationContext.TriggerReason triggerReason) {
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setProductId(product.getId());
        suggestion.setCurrentPrice(product.getCurrentPrice());

        CommerceRecommendation.PricingRecommendation pricing = recommendation.getPricingRecommendation();
        if (pricing != null) {
            suggestion.setRecommendedPrice(pricing.getRecommendedPrice());
            suggestion.setDirection(pricing.getDirection());
            suggestion.setConfidence(pricing.getConfidence());
            suggestion.setReasoning(pricing.getReasoning());
        }

        suggestion.setTriggerReason(PricingSuggestion.TriggerReason.valueOf(triggerReason.name()));
        suggestion.setStatus(PricingSuggestion.SuggestionStatus.PENDING);
        return suggestion;
    }

    private ReorderSuggestion createReorderSuggestion(
            Product product,
            CommerceRecommendation recommendation,
            RecommendationContext.TriggerReason triggerReason) {
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProductId(product.getId());
        suggestion.setCurrentStock(product.getStockLevel());

        CommerceRecommendation.ReorderRecommendation reorder = recommendation.getReorderRecommendation();
        if (reorder != null) {
            suggestion.setRecommendedQuantity(reorder.getRecommendedQuantity());
            suggestion.setConfidence(reorder.getConfidence());
            suggestion.setReasoning(reorder.getReasoning());
        }

        suggestion.setTriggerReason(ReorderSuggestion.TriggerReason.valueOf(triggerReason.name()));
        suggestion.setStatus(ReorderSuggestion.SuggestionStatus.PENDING);
        return suggestion;
    }
}