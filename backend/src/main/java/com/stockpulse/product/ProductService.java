package com.stockpulse.product;

import com.stockpulse.agent.DemandEvent;
import com.stockpulse.agent.InventoryEvent;
import com.stockpulse.commerce.CommerceRecommendation;
import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.commerce.RecommendationContext;
import com.stockpulse.product.ProductController.*;
import com.stockpulse.suggestion.PricingSuggestion;
import com.stockpulse.suggestion.ReorderSuggestion;
import com.stockpulse.suggestion.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommerceStrategyRegistry commerceStrategyRegistry;
    private final SuggestionService suggestionService;

    @Autowired
    public ProductService(ProductRepository productRepository, 
                         ApplicationEventPublisher eventPublisher,
                         CommerceStrategyRegistry commerceStrategyRegistry,
                         SuggestionService suggestionService) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.commerceStrategyRegistry = commerceStrategyRegistry;
        this.suggestionService = suggestionService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getFilteredProducts(Product.ProductLifecycle status, Product.ProductCategory category) {
        List<Product> products = productRepository.findAll();
        
        if (status != null) {
            products = products.stream()
                .filter(product -> product.getLifecycleStatus() == status)
                .collect(Collectors.toList());
        }
        
        if (category != null) {
            products = products.stream()
                .filter(product -> product.getCategory() == category)
                .collect(Collectors.toList());
        }
        
        return products;
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        // Set default values if not provided
        if (product.getDemandVelocity() == null) {
            product.setDemandVelocity(0.0);
        }
        
        if (product.getLifecycleStatus() == null) {
            product.setLifecycleStatus(Product.ProductLifecycle.ACTIVE);
        }
        
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setSku(productDetails.getSku());
            product.setName(productDetails.getName());
            product.setCategory(productDetails.getCategory());
            product.setCurrentPrice(productDetails.getCurrentPrice());
            product.setStockLevel(productDetails.getStockLevel());
            product.setReorderThreshold(productDetails.getReorderThreshold());
            product.setDemandVelocity(productDetails.getDemandVelocity());
            product.setLifecycleStatus(productDetails.getLifecycleStatus());
            product.setCostPrice(productDetails.getCostPrice());
            product.setSupplierId(productDetails.getSupplierId());
            return productRepository.save(product);
        });
    }

    public boolean deleteProduct(Long id) {
        return productRepository.findById(id).map(product -> {
            productRepository.delete(product);
            return true;
        }).orElse(false);
    }

    public Product updateStock(Long id, Integer newStockLevel) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        Integer oldStockLevel = product.getStockLevel();
        product.setStockLevel(newStockLevel);
        
        // Update lifecycle status based on stock level
        if (newStockLevel <= 0) {
            product.setLifecycleStatus(Product.ProductLifecycle.OUT_OF_STOCK);
        } else if (product.getLifecycleStatus() == Product.ProductLifecycle.OUT_OF_STOCK) {
            product.setLifecycleStatus(Product.ProductLifecycle.ACTIVE);
        }
        
        Product savedProduct = productRepository.save(product);
        
        // Publish inventory event if stock level changed significantly
        if (oldStockLevel == null || !oldStockLevel.equals(newStockLevel)) {
            eventPublisher.publishEvent(new InventoryEvent(id, newStockLevel));
        }
        
        return savedProduct;
    }

    public Product simulateOrder(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Decrement stock level
        Integer currentStock = product.getStockLevel();
        if (currentStock == null) {
            currentStock = 0;
        }
        
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        
        Integer newStockLevel = currentStock - quantity;
        product.setStockLevel(newStockLevel);
        
        // Update lifecycle status based on stock level
        if (newStockLevel <= 0) {
            product.setLifecycleStatus(Product.ProductLifecycle.OUT_OF_STOCK);
        }
        
        // Increment demand velocity
        Double currentVelocity = product.getDemandVelocity();
        if (currentVelocity == null) {
            currentVelocity = 0.0;
        }
        Double newVelocity = currentVelocity + quantity;
        product.setDemandVelocity(newVelocity);
        
        Product savedProduct = productRepository.save(product);
        
        // Publish events
        eventPublisher.publishEvent(new InventoryEvent(id, newStockLevel));
        eventPublisher.publishEvent(new DemandEvent(id, newVelocity));
        
        return savedProduct;
    }
    public PricingSuggestionResponse generatePricingSuggestion(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Create recommendation context
        RecommendationContext context = new RecommendationContext();
        context.setProductId(product.getId());
        context.setSku(product.getSku());
        context.setName(product.getName());
        context.setCategory(product.getCategory().toString());
        context.setCurrentPrice(product.getCurrentPrice());
        context.setStockLevel(product.getStockLevel());
        context.setReorderThreshold(product.getReorderThreshold());
        context.setDemandVelocity(product.getDemandVelocity());
        context.setTriggerReason(RecommendationContext.TriggerReason.MANUAL);
        
        // Generate recommendation using active strategy
        CommerceRecommendation recommendation = commerceStrategyRegistry.getActiveStrategy()
            .generateRecommendation(context);
        
        // Convert to response
        if (recommendation.getPricingRecommendation() != null) {
            CommerceRecommendation.PricingRecommendation pricingRec = recommendation.getPricingRecommendation();
            return new PricingSuggestionResponse(
                pricingRec.getRecommendedPrice(),
                pricingRec.getDirection(),
                pricingRec.getConfidence(),
                pricingRec.getReasoning()
            );
        } else {
            throw new RuntimeException("Failed to generate pricing recommendation");
        }
    }

    public ReorderSuggestionResponse generateReorderSuggestion(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Create recommendation context
        RecommendationContext context = new RecommendationContext();
        context.setProductId(product.getId());
        context.setSku(product.getSku());
        context.setName(product.getName());
        context.setCategory(product.getCategory().toString());
        context.setCurrentPrice(product.getCurrentPrice());
        context.setStockLevel(product.getStockLevel());
        context.setReorderThreshold(product.getReorderThreshold());
        context.setDemandVelocity(product.getDemandVelocity());
        context.setTriggerReason(RecommendationContext.TriggerReason.MANUAL);
        
        // Generate recommendation using active strategy
        CommerceRecommendation recommendation = commerceStrategyRegistry.getActiveStrategy()
            .generateRecommendation(context);
        
        // Convert to response
        if (recommendation.getReorderRecommendation() != null) {
            CommerceRecommendation.ReorderRecommendation reorderRec = recommendation.getReorderRecommendation();
            return new ReorderSuggestionResponse(
                reorderRec.getRecommendedQuantity(),
                reorderRec.getConfidence(),
                reorderRec.getReasoning()
            );
        } else {
            throw new RuntimeException("Failed to generate reorder recommendation");
        }
    }
}
