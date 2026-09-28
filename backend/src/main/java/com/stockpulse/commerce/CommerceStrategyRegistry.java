package com.stockpulse.commerce;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommerceStrategyRegistry {

    private final Map<String, CommerceStrategy> strategies;
    private final String activeStrategyName;

    public CommerceStrategyRegistry(
            Map<String, CommerceStrategy> strategies,
            @Value("${stockpulse.commerce.strategy:AI}") String activeStrategyName) {
        this.strategies = strategies;
        this.activeStrategyName = activeStrategyName.trim().toUpperCase();
    }

    public CommerceStrategy getActiveStrategy() {
        CommerceStrategy strategy = strategies.get(activeStrategyName);
        if (strategy != null) {
            return strategy;
        }

        return strategies.getOrDefault("RULE", strategies.values().stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No commerce strategy configured")));
    }
}
