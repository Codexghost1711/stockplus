package com.stockpulse.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    
    List<PricingSuggestion> findByProductId(Long productId);
    
    List<PricingSuggestion> findByStatus(PricingSuggestion.SuggestionStatus status);
    
    @Query("SELECT ps FROM PricingSuggestion ps WHERE ps.productId = :productId AND ps.status = :status")
    List<PricingSuggestion> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") PricingSuggestion.SuggestionStatus status);
    
    @Query("SELECT ps FROM PricingSuggestion ps WHERE ps.productId = :productId AND ps.status = 'PENDING' AND ps.triggerReason = :triggerReason")
    List<PricingSuggestion> findPendingByProductIdAndTriggerReason(@Param("productId") Long productId, @Param("triggerReason") PricingSuggestion.TriggerReason triggerReason);
}
