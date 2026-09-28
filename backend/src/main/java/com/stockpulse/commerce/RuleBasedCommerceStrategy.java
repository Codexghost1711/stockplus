package com.stockpulse.commerce;

import com.stockpulse.suggestion.PricingSuggestion;
import org.springframework.stereotype.Component;

@Component("RULE")
public class RuleBasedCommerceStrategy implements CommerceStrategy {

    @Override
    public CommerceRecommendation generateRecommendation(RecommendationContext context) {
        CommerceRecommendation recommendation = new CommerceRecommendation();
        
        // Generate pricing recommendation
        CommerceRecommendation.PricingRecommendation pricingRec = new CommerceRecommendation.PricingRecommendation();
        pricingRec.setRecommendedPrice(calculateRecommendedPrice(context));
        pricingRec.setDirection(determinePricingDirection(context));
        pricingRec.setConfidence(0.9); // High confidence for rule-based strategy
        pricingRec.setReasoning(generatePricingReasoning(context));
        
        recommendation.setPricingRecommendation(pricingRec);
        
        // Generate reorder recommendation
        CommerceRecommendation.ReorderRecommendation reorderRec = new CommerceRecommendation.ReorderRecommendation();
        reorderRec.setRecommendedQuantity(calculateReorderQuantity(context));
        reorderRec.setConfidence(0.8); // Moderate confidence for reorder calculation
        reorderRec.setReasoning(generateReorderReasoning(context));
        
        recommendation.setReorderRecommendation(reorderRec);
        
        return recommendation;
    }
    
    private Double calculateRecommendedPrice(RecommendationContext context) {
        Double currentPrice = context.getCurrentPrice();
        if (currentPrice == null) {
            currentPrice = 0.0;
        }
        
        // If stock is below reorder threshold, recommend 10% price increase
        if (context.getStockLevel() < context.getReorderThreshold()) {
            return currentPrice * 1.10;
        }
        
        // If demand velocity is more than 2x category average, recommend 5% price increase
        // For simplicity, we'll use a fixed threshold of 10 for now
        if (context.getDemandVelocity() != null && context.getDemandVelocity() > 10) {
            return currentPrice * 1.05;
        }
        
        // Otherwise, hold the current price
        return currentPrice;
    }
    
    private PricingSuggestion.PricingDirection determinePricingDirection(RecommendationContext context) {
        Double currentPrice = context.getCurrentPrice();
        Double recommendedPrice = calculateRecommendedPrice(context);
        
        if (recommendedPrice > currentPrice) {
            return PricingSuggestion.PricingDirection.INCREASE;
        } else if (recommendedPrice < currentPrice) {
            return PricingSuggestion.PricingDirection.DECREASE;
        } else {
            return PricingSuggestion.PricingDirection.HOLD;
        }
    }
    
    private String generatePricingReasoning(RecommendationContext context) {
        if (context.getStockLevel() < context.getReorderThreshold()) {
            return "Stock level (" + context.getStockLevel() + ") is below reorder threshold (" + 
                   context.getReorderThreshold() + "). Recommended 10% price increase to optimize revenue.";
        }
        
        if (context.getDemandVelocity() != null && context.getDemandVelocity() > 10) {
            return "High demand velocity (" + context.getDemandVelocity() + ") detected. " +
                   "Recommended 5% price increase to capitalize on trend.";
        }
        
        return "No significant factors affecting pricing. Current price is optimal.";
    }
    
    private Integer calculateReorderQuantity(RecommendationContext context) {
        // Rule: recommend quantity = (reorder threshold * 3) - current stock, minimum 1
        int recommended = (context.getReorderThreshold() * 3) - context.getStockLevel();
        return Math.max(recommended, 1);
    }
    
    private String generateReorderReasoning(RecommendationContext context) {
        int recommendedQty = calculateReorderQuantity(context);
        return "Calculated reorder quantity using formula: (reorder threshold * 3) - current stock = " +
               "(" + context.getReorderThreshold() + " * 3) - " + context.getStockLevel() + " = " + 
               recommendedQty + ". Minimum order quantity enforced.";
    }
}
