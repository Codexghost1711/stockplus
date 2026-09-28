package com.stockpulse.agent;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.stockpulse.product.Product;
import com.stockpulse.product.ProductRepository;
import com.stockpulse.product.ProductService;
import com.stockpulse.suggestion.PricingSuggestion;
import com.stockpulse.suggestion.PricingSuggestionRepository;
import com.stockpulse.suggestion.ReorderSuggestion;
import com.stockpulse.suggestion.ReorderSuggestionRepository;

@SpringBootTest(properties = {
        "stockpulse.ai.provider=UNKNOWN",
        "stockpulse.commerce.strategy=RULE",
        "spring.datasource.url=jdbc:h2:mem:stockpulse-flow-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class RecommendationFlowIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;

    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;

    @Test
    void simulateOrderBelowThreshold_PersistsPricingAndReorderSuggestions() {
        Product product = new Product();
        product.setSku("FLOW-TEST-001");
        product.setName("Event flow test product");
        product.setCategory(Product.ProductCategory.HOME);
        product.setCurrentPrice(25.0);
        product.setStockLevel(6);
        product.setReorderThreshold(5);
        product.setDemandVelocity(0.0);
        Long productId = productRepository.save(product).getId();

        productService.simulateOrder(productId, 2);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<PricingSuggestion> pricingSuggestions = pricingSuggestionRepository.findByProductId(productId);
            List<ReorderSuggestion> reorderSuggestions = reorderSuggestionRepository.findByProductId(productId);

            assertFalse(pricingSuggestions.isEmpty());
            assertFalse(reorderSuggestions.isEmpty());
            assertEquals(PricingSuggestion.TriggerReason.INVENTORY_LOW,
                    pricingSuggestions.get(0).getTriggerReason());
            assertEquals(ReorderSuggestion.TriggerReason.INVENTORY_LOW,
                    reorderSuggestions.get(0).getTriggerReason());
            assertEquals(PricingSuggestion.SuggestionStatus.PENDING,
                    pricingSuggestions.get(0).getStatus());
            assertEquals(ReorderSuggestion.SuggestionStatus.PENDING,
                    reorderSuggestions.get(0).getStatus());
        });
    }

        @Test
        void suggestionReasoningLongerThan255Characters_PersistsForBothSuggestionTypes() {
                String longReasoning = "AI recommendation reasoning ".repeat(30);

                PricingSuggestion pricingSuggestion = new PricingSuggestion();
                pricingSuggestion.setProductId(1L);
                pricingSuggestion.setReasoning(longReasoning);
                PricingSuggestion savedPricing = pricingSuggestionRepository.save(pricingSuggestion);

                ReorderSuggestion reorderSuggestion = new ReorderSuggestion();
                reorderSuggestion.setProductId(1L);
                reorderSuggestion.setReasoning(longReasoning);
                ReorderSuggestion savedReorder = reorderSuggestionRepository.save(reorderSuggestion);

                assertEquals(longReasoning, pricingSuggestionRepository.findById(savedPricing.getId())
                                .orElseThrow().getReasoning());
                assertEquals(longReasoning, reorderSuggestionRepository.findById(savedReorder.getId())
                                .orElseThrow().getReasoning());
        }
}
