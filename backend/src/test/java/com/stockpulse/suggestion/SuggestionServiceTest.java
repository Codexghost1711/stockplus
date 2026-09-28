package com.stockpulse.suggestion;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.product.Product;
import com.stockpulse.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private PricingSuggestionRepository pricingSuggestionRepository;

    @Mock
    private ReorderSuggestionRepository reorderSuggestionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CommerceStrategyRegistry commerceStrategyRegistry;

        @InjectMocks
        private SuggestionService suggestionService;

    @Test
    void updatePricingSuggestionStatus_AcceptedWithValidPrice_ShouldUpdateProductPrice() {
        Long suggestionId = 1L;
        Long productId = 10L;
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setId(suggestionId);
        suggestion.setProductId(productId);
        suggestion.setRecommendedPrice(15.99);
        suggestion.setStatus(PricingSuggestion.SuggestionStatus.PENDING);

        Product product = new Product();
        product.setId(productId);
        product.setCurrentPrice(10.0);

        when(pricingSuggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(pricingSuggestionRepository.save(any(PricingSuggestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PricingSuggestion updated = suggestionService.updatePricingSuggestionStatus(
                suggestionId, PricingSuggestion.SuggestionStatus.ACCEPTED);

        assertEquals(PricingSuggestion.SuggestionStatus.ACCEPTED, updated.getStatus());
        assertEquals(15.99, product.getCurrentPrice());
        verify(productRepository).save(product);
    }

    @Test
    void updatePricingSuggestionStatus_AlreadyProcessed_ShouldThrowException() {
        Long suggestionId = 1L;
        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setId(suggestionId);
        suggestion.setStatus(PricingSuggestion.SuggestionStatus.ACCEPTED);
        when(pricingSuggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                suggestionService.updatePricingSuggestionStatus(
                        suggestionId, PricingSuggestion.SuggestionStatus.REJECTED));

        assertTrue(exception.getMessage().contains("already processed suggestion"));
        verify(pricingSuggestionRepository, never()).save(any(PricingSuggestion.class));
    }

    @Test
    void updateReorderSuggestionStatus_AcceptedWithValidQuantity_ShouldUpdateProductStock() {
        Long suggestionId = 1L;
        Long productId = 10L;
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setId(suggestionId);
        suggestion.setProductId(productId);
        suggestion.setRecommendedQuantity(50);
        suggestion.setStatus(ReorderSuggestion.SuggestionStatus.PENDING);

        Product product = new Product();
        product.setId(productId);
        product.setStockLevel(10);

        when(reorderSuggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reorderSuggestionRepository.save(any(ReorderSuggestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReorderSuggestion updated = suggestionService.updateReorderSuggestionStatus(
                suggestionId, ReorderSuggestion.SuggestionStatus.ACCEPTED);

        assertEquals(ReorderSuggestion.SuggestionStatus.ACCEPTED, updated.getStatus());
        assertEquals(60, product.getStockLevel());
        verify(productRepository).save(product);
    }

    @Test
    void updateReorderSuggestionStatus_AlreadyProcessed_ShouldThrowException() {
        Long suggestionId = 1L;
        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setId(suggestionId);
        suggestion.setStatus(ReorderSuggestion.SuggestionStatus.REJECTED);
        when(reorderSuggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                suggestionService.updateReorderSuggestionStatus(
                        suggestionId, ReorderSuggestion.SuggestionStatus.ACCEPTED));

        assertTrue(exception.getMessage().contains("already processed suggestion"));
        verify(reorderSuggestionRepository, never()).save(any(ReorderSuggestion.class));
    }
}