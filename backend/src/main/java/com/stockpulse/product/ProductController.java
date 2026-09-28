package com.stockpulse.product;

import com.stockpulse.common.dto.ApiResponse;
import com.stockpulse.suggestion.PricingSuggestion;
import com.stockpulse.suggestion.ReorderSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * POST /products - create product with initial stock and price
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.ok(new ApiResponse<>(true, "Product created successfully", createdProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * GET /products?status=&category= - filterable catalog list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getProducts(
            @RequestParam(required = false) Product.ProductLifecycle status,
            @RequestParam(required = false) Product.ProductCategory category) {
        try {
            List<Product> products = productService.getFilteredProducts(status, category);
            return ResponseEntity.ok(new ApiResponse<>(true, "Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * PATCH /products/{id}/stock - update stock level; fires agentic loop if below reorder threshold
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> updateStock(
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {
        try {
            Product updatedProduct = productService.updateStock(id, request.getStockLevel());
            return ResponseEntity.ok(new ApiResponse<>(true, "Stock updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * POST /products/{id}/orders - simulate a sale (decrements stock, bumps demand velocity); 
     * may fire loop on spike or low stock
     */
    @PostMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<Product>> simulateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequest request) {
        try {
            Product updatedProduct = productService.simulateOrder(id, request.getQuantity());
            return ResponseEntity.ok(new ApiResponse<>(true, "Order processed successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * POST /products/{id}/suggest-pricing - on-demand pricing suggestion
     */
    @PostMapping("/{id}/suggest-pricing")
    public ResponseEntity<ApiResponse<PricingSuggestionResponse>> suggestPricing(@PathVariable Long id) {
        try {
            PricingSuggestionResponse response = productService.generatePricingSuggestion(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pricing suggestion generated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * POST /products/{id}/suggest-reorder - on-demand reorder suggestion
     */
    @PostMapping("/{id}/suggest-reorder")
    public ResponseEntity<ApiResponse<ReorderSuggestionResponse>> suggestReorder(@PathVariable Long id) {
        try {
            ReorderSuggestionResponse response = productService.generateReorderSuggestion(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reorder suggestion generated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // Request DTO for stock update
    public static class StockUpdateRequest {
        private Integer stockLevel;

        public Integer getStockLevel() {
            return stockLevel;
        }

        public void setStockLevel(Integer stockLevel) {
            this.stockLevel = stockLevel;
        }
    }

    public static class OrderRequest {
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class PricingSuggestionResponse {
        private Double recommendedPrice;
        private PricingSuggestion.PricingDirection direction;
        private Double confidence;
        private String reasoning;

        public PricingSuggestionResponse() {}

        public PricingSuggestionResponse(Double recommendedPrice, PricingSuggestion.PricingDirection direction, 
                                       Double confidence, String reasoning) {
            this.recommendedPrice = recommendedPrice;
            this.direction = direction;
            this.confidence = confidence;
            this.reasoning = reasoning;
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
    }

    public static class ReorderSuggestionResponse {
        private Integer recommendedQuantity;
        private Double confidence;
        private String reasoning;

        public ReorderSuggestionResponse() {}

        public ReorderSuggestionResponse(Integer recommendedQuantity, Double confidence, String reasoning) {
            this.recommendedQuantity = recommendedQuantity;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }

        public Integer getRecommendedQuantity() {
            return recommendedQuantity;
        }

        public void setRecommendedQuantity(Integer recommendedQuantity) {
            this.recommendedQuantity = recommendedQuantity;
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
    }
}
