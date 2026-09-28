package com.stockpulse.suggestion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockpulse.common.dto.ApiResponse;

@RestController
@RequestMapping("/products")
public class SuggestionController {

    private final SuggestionService suggestionService;

    @Autowired
    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PatchMapping("/pricing-suggestions/{id}")
    public ResponseEntity<ApiResponse<PricingSuggestion>> updatePricingSuggestionStatus(
            @PathVariable Long id, 
            @RequestBody SuggestionStatusUpdateRequest request) {
        
        try {
            PricingSuggestion updatedSuggestion = 
                suggestionService.updatePricingSuggestionStatus(id, request.getStatus());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Pricing suggestion updated", updatedSuggestion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/reorder-suggestions/{id}")
    public ResponseEntity<ApiResponse<ReorderSuggestion>> updateReorderSuggestionStatus(
            @PathVariable Long id, 
            @RequestBody SuggestionStatusUpdateRequest request) {
        
        try {
            ReorderSuggestion updatedSuggestion = 
                suggestionService.updateReorderSuggestionStatus(
                    id, ReorderSuggestion.SuggestionStatus.valueOf(request.getStatus().name()));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Reorder suggestion updated", updatedSuggestion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/pricing-suggestions")
    public ResponseEntity<ApiResponse<List<PricingSuggestion>>> getPricingSuggestions(@PathVariable Long id) {
        try {
            List<PricingSuggestion> suggestions = suggestionService.getPricingSuggestionsByProductId(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pricing suggestions retrieved", suggestions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/reorder-suggestions")
    public ResponseEntity<ApiResponse<List<ReorderSuggestion>>> getReorderSuggestions(@PathVariable Long id) {
        try {
            List<ReorderSuggestion> suggestions = suggestionService.getReorderSuggestionsByProductId(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reorder suggestions retrieved", suggestions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // Request DTO for status update
    public static class SuggestionStatusUpdateRequest {
        private PricingSuggestion.SuggestionStatus status;

        public PricingSuggestion.SuggestionStatus getStatus() {
            return status;
        }

        public void setStatus(PricingSuggestion.SuggestionStatus status) {
            this.status = status;
        }
    }
}
