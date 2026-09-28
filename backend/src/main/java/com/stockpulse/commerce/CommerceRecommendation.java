package com.stockpulse.commerce;

import com.stockpulse.suggestion.PricingSuggestion;

public class CommerceRecommendation {

    private PricingRecommendation pricingRecommendation;
    private ReorderRecommendation reorderRecommendation;

    public CommerceRecommendation() {
    }

    public CommerceRecommendation(PricingRecommendation pricingRecommendation, ReorderRecommendation reorderRecommendation) {
        this.pricingRecommendation = pricingRecommendation;
        this.reorderRecommendation = reorderRecommendation;
    }

    public PricingRecommendation getPricingRecommendation() {
        return pricingRecommendation;
    }

    public void setPricingRecommendation(PricingRecommendation pricingRecommendation) {
        this.pricingRecommendation = pricingRecommendation;
    }

    public ReorderRecommendation getReorderRecommendation() {
        return reorderRecommendation;
    }

    public void setReorderRecommendation(ReorderRecommendation reorderRecommendation) {
        this.reorderRecommendation = reorderRecommendation;
    }

    public static class PricingRecommendation {
        private Long productId;
        private Double currentPrice;
        private Double recommendedPrice;
        private PricingSuggestion.PricingDirection direction;
        private Double confidence;
        private String reasoning;
        private String status;
        private String triggerReason;

        public PricingRecommendation() {
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Double getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(Double currentPrice) {
            this.currentPrice = currentPrice;
        }

        public Double getRecommendedPrice() {
            return recommendedPrice;
        }

        public void setRecommendedPrice(Double recommendedPrice) {
            this.recommendedPrice = recommendedPrice;
        }

        public PricingSuggestion.PricingDirection getDirection() {
            return direction;
        }

        public void setDirection(PricingSuggestion.PricingDirection direction) {
            this.direction = direction;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public String getReasoning() {
            return reasoning;
        }

        public void setReasoning(String reasoning) {
            this.reasoning = reasoning;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTriggerReason() {
            return triggerReason;
        }

        public void setTriggerReason(String triggerReason) {
            this.triggerReason = triggerReason;
        }
    }

    public static class ReorderRecommendation {
        private Long productId;
        private Integer currentStock;
        private Integer recommendedQuantity;
        private Integer suggestedLeadTimeDays;
        private Double confidence;
        private String reasoning;
        private String status;
        private String triggerReason;

        public ReorderRecommendation() {
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(Integer currentStock) {
            this.currentStock = currentStock;
        }

        public Integer getRecommendedQuantity() {
            return recommendedQuantity;
        }

        public void setRecommendedQuantity(Integer recommendedQuantity) {
            this.recommendedQuantity = recommendedQuantity;
        }

        public Integer getSuggestedLeadTimeDays() {
            return suggestedLeadTimeDays;
        }

        public void setSuggestedLeadTimeDays(Integer suggestedLeadTimeDays) {
            this.suggestedLeadTimeDays = suggestedLeadTimeDays;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public String getReasoning() {
            return reasoning;
        }

        public void setReasoning(String reasoning) {
            this.reasoning = reasoning;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTriggerReason() {
            return triggerReason;
        }

        public void setTriggerReason(String triggerReason) {
            this.triggerReason = triggerReason;
        }
    }
}
