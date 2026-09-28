package com.stockpulse.commerce;

import org.springframework.stereotype.Component;

@Component
public class CommerceAdvisor {

    private final CommerceStrategyRegistry strategyRegistry;

    public CommerceAdvisor(CommerceStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
    }

    public CommerceRecommendation recommend(RecommendationContext context) {
        CommerceStrategy strategy = strategyRegistry.getActiveStrategy();
        return strategy.generateRecommendation(context);
    }
}
