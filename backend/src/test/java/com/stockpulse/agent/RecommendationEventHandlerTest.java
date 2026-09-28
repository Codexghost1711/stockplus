package com.stockpulse.agent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockpulse.commerce.CommerceRecommendation;
import com.stockpulse.commerce.CommerceStrategy;
import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.commerce.RecommendationContext;
import com.stockpulse.product.Product;
import com.stockpulse.product.ProductRepository;
import com.stockpulse.suggestion.PricingSuggestion;
import com.stockpulse.suggestion.ReorderSuggestion;
import com.stockpulse.suggestion.SuggestionService;

@ExtendWith(MockitoExtension.class)
class RecommendationEventHandlerTest {

    private static final Long PRODUCT_ID = 42L;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SuggestionService suggestionService;

    @Mock
    private CommerceStrategyRegistry commerceStrategyRegistry;

    @Mock
    private CommerceStrategy commerceStrategy;

    @InjectMocks
    private RecommendationEventHandler handler;

    @Test
    void handleInventoryEvent_WhenStockIsLow_CreatesTriggeredSuggestions() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(commerceStrategyRegistry.getActiveStrategy()).thenReturn(commerceStrategy);
        when(commerceStrategy.generateRecommendation(any(RecommendationContext.class)))
                .thenReturn(recommendation());

        handler.handleInventoryEvent(new InventoryEvent(PRODUCT_ID, 4));

        ArgumentCaptor<RecommendationContext> contextCaptor = ArgumentCaptor.forClass(RecommendationContext.class);
        verify(commerceStrategy).generateRecommendation(contextCaptor.capture());
        assertEquals(RecommendationContext.TriggerReason.INVENTORY_LOW, contextCaptor.getValue().getTriggerReason());

        ArgumentCaptor<PricingSuggestion> pricingCaptor = ArgumentCaptor.forClass(PricingSuggestion.class);
        ArgumentCaptor<ReorderSuggestion> reorderCaptor = ArgumentCaptor.forClass(ReorderSuggestion.class);
        verify(suggestionService).createPricingSuggestion(pricingCaptor.capture());
        verify(suggestionService).createReorderSuggestion(reorderCaptor.capture());
        assertEquals(PricingSuggestion.TriggerReason.INVENTORY_LOW, pricingCaptor.getValue().getTriggerReason());
        assertEquals(ReorderSuggestion.TriggerReason.INVENTORY_LOW, reorderCaptor.getValue().getTriggerReason());
    }

    @Test
    void handleDemandEvent_WhenDemandSpikes_CreatesTriggeredSuggestions() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(commerceStrategyRegistry.getActiveStrategy()).thenReturn(commerceStrategy);
        when(commerceStrategy.generateRecommendation(any(RecommendationContext.class)))
                .thenReturn(recommendation());

        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 16.0));

        ArgumentCaptor<RecommendationContext> contextCaptor = ArgumentCaptor.forClass(RecommendationContext.class);
        verify(commerceStrategy).generateRecommendation(contextCaptor.capture());
        assertEquals(RecommendationContext.TriggerReason.DEMAND_SPIKE, contextCaptor.getValue().getTriggerReason());
        verify(suggestionService).createPricingSuggestion(any(PricingSuggestion.class));
        verify(suggestionService).createReorderSuggestion(any(ReorderSuggestion.class));
    }

    @Test
    void handleDemandEvent_TriggersAgainOnlyAfterDemandFallsAndCrossesThreshold() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(commerceStrategyRegistry.getActiveStrategy()).thenReturn(commerceStrategy);
        when(commerceStrategy.generateRecommendation(any(RecommendationContext.class)))
                .thenReturn(recommendation());

        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 15.0));
        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 16.0));
        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 17.0));
        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 10.0));
        handler.handleDemandEvent(new DemandEvent(PRODUCT_ID, 16.0));

        verify(commerceStrategy, times(2)).generateRecommendation(any(RecommendationContext.class));
        verify(suggestionService, times(2)).createPricingSuggestion(any(PricingSuggestion.class));
        verify(suggestionService, times(2)).createReorderSuggestion(any(ReorderSuggestion.class));
    }

    @Test
    void handleInventoryEvent_WhenSuggestionsAreAlreadyPending_SkipsGeneration() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(suggestionService.hasPendingSuggestions(PRODUCT_ID, PricingSuggestion.TriggerReason.INVENTORY_LOW))
                .thenReturn(true);

        handler.handleInventoryEvent(new InventoryEvent(PRODUCT_ID, 4));

        verify(commerceStrategyRegistry, never()).getActiveStrategy();
        verify(suggestionService, never()).createPricingSuggestion(any(PricingSuggestion.class));
        verify(suggestionService, never()).createReorderSuggestion(any(ReorderSuggestion.class));
    }

    private Product product() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setSku("SKU-42");
        product.setName("Test product");
        product.setCategory(Product.ProductCategory.ELECTRONICS);
        product.setCurrentPrice(20.0);
        product.setStockLevel(4);
        product.setReorderThreshold(5);
        product.setDemandVelocity(2.0);
        return product;
    }

    private CommerceRecommendation recommendation() {
        CommerceRecommendation.PricingRecommendation pricing = new CommerceRecommendation.PricingRecommendation();
        pricing.setRecommendedPrice(22.0);
        pricing.setDirection(PricingSuggestion.PricingDirection.INCREASE);
        pricing.setConfidence(0.9);
        pricing.setReasoning("Low inventory");

        CommerceRecommendation.ReorderRecommendation reorder = new CommerceRecommendation.ReorderRecommendation();
        reorder.setRecommendedQuantity(11);
        reorder.setConfidence(0.8);
        reorder.setReasoning("Restock soon");
        return new CommerceRecommendation(pricing, reorder);
    }
}
