package com.stockpulse.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    
    List<ReorderSuggestion> findByProductId(Long productId);
    
    List<ReorderSuggestion> findByStatus(ReorderSuggestion.SuggestionStatus status);
    
    @Query("SELECT rs FROM ReorderSuggestion rs WHERE rs.productId = :productId AND rs.status = :status")
    List<ReorderSuggestion> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") ReorderSuggestion.SuggestionStatus status);
    
    @Query("SELECT rs FROM ReorderSuggestion rs WHERE rs.productId = :productId AND rs.status = 'PENDING' AND rs.triggerReason = :triggerReason")
    List<ReorderSuggestion> findPendingByProductIdAndTriggerReason(@Param("productId") Long productId, @Param("triggerReason") ReorderSuggestion.TriggerReason triggerReason);
}
