import React, { useState, useEffect } from 'react';
import { stockpulseApi } from '../api/stockpulseApi';
import { PricingSuggestion, ReorderSuggestion } from './SuggestionCards';

export const SuggestionSection = ({ productId, onProductUpdate }) => {
  const [pricingSuggestions, setPricingSuggestions] = useState([]);
  const [reorderSuggestions, setReorderSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchSuggestions();
    
    // Poll for suggestions every 2 seconds for 30 seconds after component mounts
    const pollInterval = setInterval(fetchSuggestions, 2000);
    const timeout = setTimeout(() => clearInterval(pollInterval), 30000);
    
    return () => {
      clearInterval(pollInterval);
      clearTimeout(timeout);
    };
  }, [productId]);

  const fetchSuggestions = async () => {
    try {
      setLoading(true);
      const [pricingResponse, reorderResponse] = await Promise.all([
        stockpulseApi.getPricingSuggestions(productId),
        stockpulseApi.getReorderSuggestions(productId)
      ]);
      
      if (pricingResponse.success) {
        // Filter for pending suggestions only
        const pendingPricing = pricingResponse.data.filter(s => s.status === 'PENDING');
        setPricingSuggestions(pendingPricing);
      }
      
      if (reorderResponse.success) {
        // Filter for pending suggestions only
        const pendingReorder = reorderResponse.data.filter(s => s.status === 'PENDING');
        setReorderSuggestions(pendingReorder);
      }
    } catch (err) {
      console.error('Failed to fetch suggestions:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSuggestionAction = async (type, suggestionId, action) => {
    try {
      // Convert action to proper enum value
      const status = action === 'accept' ? 'ACCEPTED' : 'REJECTED';
      
      let response;
      if (type === 'pricing') {
        response = await stockpulseApi.updatePricingSuggestionStatus(suggestionId, status);
      } else {
        response = await stockpulseApi.updateReorderSuggestionStatus(suggestionId, status);
      }
      
      if (response.success) {
        // Refresh suggestions
        fetchSuggestions();
        
        // Notify parent to refresh product data since it might have changed
        if (onProductUpdate) {
          onProductUpdate();
        }
        
        alert(`Suggestion ${action}ed successfully`);
      } else {
        alert(`Failed to ${action} suggestion: ${response.message}`);
      }
    } catch (err) {
      alert('Failed to connect to backend');
    }
  };

  if (loading && (pricingSuggestions.length === 0 && reorderSuggestions.length === 0)) {
    return <div className="suggestions-loading">Checking for suggestions...</div>;
  }

  if (pricingSuggestions.length === 0 && reorderSuggestions.length === 0) {
    return null;
  }

  return (
    <div className="suggestions-section">
      <h4>Pending Recommendations</h4>
      
      {pricingSuggestions.map((suggestion) => (
        <PricingSuggestion 
          key={suggestion.id} 
          suggestion={suggestion} 
          onAction={(action) => handleSuggestionAction('pricing', suggestion.id, action)}
        />
      ))}
      
      {reorderSuggestions.map((suggestion) => (
        <ReorderSuggestion 
          key={suggestion.id} 
          suggestion={suggestion} 
          onAction={(action) => handleSuggestionAction('reorder', suggestion.id, action)}
        />
      ))}
    </div>
  );
};