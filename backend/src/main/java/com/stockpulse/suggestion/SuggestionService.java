package com.stockpulse.suggestion;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stockpulse.commerce.CommerceRecommendation;
import com.stockpulse.commerce.CommerceStrategy;
import com.stockpulse.commerce.CommerceStrategyRegistry;
import com.stockpulse.commerce.RecommendationContext;
import com.stockpulse.product.Product;
import com.stockpulse.product.ProductRepository;

@Service
public class SuggestionService {

    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;
    private final ProductRepository productRepository;
    private final CommerceStrategyRegistry commerceStrategyRegistry;

    @Autowired
    public SuggestionService(
            PricingSuggestionRepository pricingSuggestionRepository,
            ReorderSuggestionRepository reorderSuggestionRepository,
            ProductRepository productRepository,
            CommerceStrategyRegistry commerceStrategyRegistry) {
        this.pricingSuggestionRepository = pricingSuggestionRepository;
        this.reorderSuggestionRepository = reorderSuggestionRepository;
        this.productRepository = productRepository;
        this.commerceStrategyRegistry = commerceStrategyRegistry;
    }

    public List<PricingSuggestion> getPricingSuggestionsByProductId(Long productId) {
        return pricingSuggestionRepository.findByProductId(productId);
    }

    public List<ReorderSuggestion> getReorderSuggestionsByProductId(Long productId) {
        return reorderSuggestionRepository.findByProductId(productId);
    }

    public Optional<PricingSuggestion> getPricingSuggestionById(Long id) {
        return pricingSuggestionRepository.findById(id);
    }

    public Optional<ReorderSuggestion> getReorderSuggestionById(Long id) {
        return reorderSuggestionRepository.findById(id);
    }

    public PricingSuggestion createPricingSuggestion(PricingSuggestion suggestion) {
        return pricingSuggestionRepository.save(suggestion);
    }

    public ReorderSuggestion createReorderSuggestion(ReorderSuggestion suggestion) {
        return reorderSuggestionRepository.save(suggestion);
    }

    public PricingSuggestion updatePricingSuggestionStatus(Long id, PricingSuggestion.SuggestionStatus status) {
        PricingSuggestion suggestion = pricingSuggestionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pricing suggestion not found with id: " + id));
        
        // Prevent updating already processed suggestions
        if (suggestion.getStatus() != PricingSuggestion.SuggestionStatus.PENDING) {
            throw new RuntimeException("Cannot update status of already processed suggestion. Current status: " + suggestion.getStatus());
        }
        
        // If accepting a pricing suggestion, update the product price
        if (status == PricingSuggestion.SuggestionStatus.ACCEPTED && 
            suggestion.getRecommendedPrice() != null) {
            Optional<Product> productOpt = productRepository.findById(suggestion.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setCurrentPrice(suggestion.getRecommendedPrice());
                product.setLifecycleStatus(Product.ProductLifecycle.ACTIVE);
                productRepository.save(product);
            }
        }
        
        suggestion.setStatus(status);
        return pricingSuggestionRepository.save(suggestion);
    }

    public ReorderSuggestion updateReorderSuggestionStatus(Long id, ReorderSuggestion.SuggestionStatus status) {
        ReorderSuggestion suggestion = reorderSuggestionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reorder suggestion not found with id: " + id));
        
        // Prevent updating already processed suggestions
        if (suggestion.getStatus() != ReorderSuggestion.SuggestionStatus.PENDING) {
            throw new RuntimeException("Cannot update status of already processed suggestion. Current status: " + suggestion.getStatus());
        }
        
        // If accepting a reorder suggestion, update the product stock level
        if (status == ReorderSuggestion.SuggestionStatus.ACCEPTED && 
            suggestion.getRecommendedQuantity() != null) {
            Optional<Product> productOpt = productRepository.findById(suggestion.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                Integer currentStock = product.getStockLevel();
                if (currentStock == null) {
                    currentStock = 0;
                }
                product.setStockLevel(currentStock + suggestion.getRecommendedQuantity());
                
                // Update lifecycle status if it was previously OUT_OF_STOCK
                if (product.getLifecycleStatus() == Product.ProductLifecycle.OUT_OF_STOCK && 
                    product.getStockLevel() > 0) {
                    product.setLifecycleStatus(Product.ProductLifecycle.ACTIVE);
                }
                
                productRepository.save(product);
            }
        }
        
        suggestion.setStatus(status);
        return reorderSuggestionRepository.save(suggestion);
    }

    public boolean deletePricingSuggestion(Long id) {
        return pricingSuggestionRepository.findById(id).map(suggestion -> {
            pricingSuggestionRepository.delete(suggestion);
            return true;
        }).orElse(false);
    }

    public boolean deleteReorderSuggestion(Long id) {
        return reorderSuggestionRepository.findById(id).map(suggestion -> {
            reorderSuggestionRepository.delete(suggestion);
            return true;
        }).orElse(false);
    }

    public CommerceRecommendation generateRecommendations(Long productId, RecommendationContext.TriggerReason triggerReason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Product product = productOpt.get();
        
        // Create recommendation context
        RecommendationContext context = new RecommendationContext();
        context.setProductId(product.getId());
        context.setSku(product.getSku());
        context.setName(product.getName());
        context.setCategory(product.getCategory().name());
        context.setCurrentPrice(product.getCurrentPrice());
        context.setStockLevel(product.getStockLevel());
        context.setReorderThreshold(product.getReorderThreshold());
        context.setDemandVelocity(product.getDemandVelocity());
        context.setTriggerReason(triggerReason);

        // Get the active commerce strategy
        CommerceStrategy strategy = commerceStrategyRegistry.getActiveStrategy();
        
        // Generate recommendations
        return strategy.generateRecommendation(context);
    }

    public boolean hasPendingSuggestions(Long productId, PricingSuggestion.TriggerReason triggerReason) {
        List<PricingSuggestion> pricingSuggestions = 
            pricingSuggestionRepository.findPendingByProductIdAndTriggerReason(productId, triggerReason);
        List<ReorderSuggestion> reorderSuggestions = 
            reorderSuggestionRepository.findPendingByProductIdAndTriggerReason(
                productId, ReorderSuggestion.TriggerReason.valueOf(triggerReason.name()));
        
        return !pricingSuggestions.isEmpty() || !reorderSuggestions.isEmpty();
    }
}
