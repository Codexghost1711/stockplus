package com.stockpulse.commerce;

public interface CommerceStrategy {

    CommerceRecommendation generateRecommendation(RecommendationContext context);
}
