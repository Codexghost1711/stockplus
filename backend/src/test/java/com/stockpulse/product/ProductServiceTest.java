package com.stockpulse.product;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.stockpulse.agent.DemandEvent;
import com.stockpulse.agent.InventoryEvent;
import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.suggestion.SuggestionService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CommerceStrategyRegistry commerceStrategyRegistry;

    @Mock
    private SuggestionService suggestionService;

    @InjectMocks
    private ProductService productService;

    @Test
    void simulateOrder_ShouldReduceStockAndIncreaseDemandVelocity() {
        Long productId = 1L;
        Product product = product(productId, 10, 5.0);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productService.simulateOrder(productId, 3);

        assertNotNull(updatedProduct);
        assertEquals(7, updatedProduct.getStockLevel());
        assertEquals(8.0, updatedProduct.getDemandVelocity());
        verify(productRepository).save(product);
    }

    @Test
    void simulateOrder_ShouldPublishInventoryAndDemandEvents() {
        Long productId = 1L;
        Product product = product(productId, 10, 5.0);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.simulateOrder(productId, 3);

        ArgumentCaptor<InventoryEvent> inventoryCaptor = ArgumentCaptor.forClass(InventoryEvent.class);
        ArgumentCaptor<DemandEvent> demandCaptor = ArgumentCaptor.forClass(DemandEvent.class);
        verify(eventPublisher).publishEvent(inventoryCaptor.capture());
        verify(eventPublisher).publishEvent(demandCaptor.capture());
        assertEquals(productId, inventoryCaptor.getValue().getProductId());
        assertEquals(7, inventoryCaptor.getValue().getStockLevel());
        assertEquals(productId, demandCaptor.getValue().getProductId());
        assertEquals(8.0, demandCaptor.getValue().getDemandVelocity());
    }

    @Test
    void simulateOrder_WithInsufficientStock_ShouldThrowException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product(productId, 10, 5.0)));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.simulateOrder(productId, 15));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void simulateOrder_WithNonExistentProduct_ShouldThrowException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.simulateOrder(productId, 3));

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateStock_BelowReorderThreshold_ShouldPublishEvent() {
        Long productId = 1L;
        Product product = product(productId, 10, 5.0);
        product.setReorderThreshold(5);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct = productService.updateStock(productId, 3);

        assertNotNull(updatedProduct);
        assertEquals(3, updatedProduct.getStockLevel());
        ArgumentCaptor<InventoryEvent> eventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(productId, eventCaptor.getValue().getProductId());
        assertEquals(3, eventCaptor.getValue().getStockLevel());
    }

    private Product product(Long id, Integer stockLevel, Double demandVelocity) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setStockLevel(stockLevel);
        product.setDemandVelocity(demandVelocity);
        return product;
    }
}