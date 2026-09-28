export const PricingSuggestion = ({ suggestion, onAction }) => {
  return (
    <div className="suggestion-card pricing-suggestion">
      <div className="suggestion-header">
        <span className="trigger-badge">{suggestion.triggerReason.replace('_', ' ')}</span>
        <h5>Pricing Recommendation</h5>
      </div>
      
      <div className="suggestion-details">
        <div className="detail-row">
          <span className="label">Current Price:</span>
          <span className="value">${suggestion.currentPrice?.toFixed(2) || '0.00'}</span>
        </div>
        <div className="detail-row">
          <span className="label">Recommended Price:</span>
          <span className="value">${suggestion.recommendedPrice?.toFixed(2) || '0.00'}</span>
        </div>
        <div className="detail-row">
          <span className="label">Direction:</span>
          <span className="value">{suggestion.direction}</span>
        </div>
        <div className="detail-row">
          <span className="label">Confidence:</span>
          <span className="value">{(suggestion.confidence * 100).toFixed(0)}%</span>
        </div>
        <div className="detail-row">
          <span className="label">Reasoning:</span>
          <span className="value reasoning-text">{suggestion.reasoning}</span>
        </div>
      </div>
      
      <div className="suggestion-actions">
        <button 
          className="accept-btn" 
          onClick={() => onAction('accept')}
        >
          Accept Price
        </button>
        <button 
          className="reject-btn" 
          onClick={() => onAction('reject')}
        >
          Reject
        </button>
      </div>
    </div>
  );
};

export const ReorderSuggestion = ({ suggestion, onAction }) => {
  return (
    <div className="suggestion-card reorder-suggestion">
      <div className="suggestion-header">
        <span className="trigger-badge">{suggestion.triggerReason.replace('_', ' ')}</span>
        <h5>Reorder Recommendation</h5>
      </div>
      
      <div className="suggestion-details">
        <div className="detail-row">
          <span className="label">Current Stock:</span>
          <span className="value">{suggestion.currentStock}</span>
        </div>
        <div className="detail-row">
          <span className="label">Recommended Quantity:</span>
          <span className="value">{suggestion.recommendedQuantity}</span>
        </div>
        <div className="detail-row">
          <span className="label">Confidence:</span>
          <span className="value">{(suggestion.confidence * 100).toFixed(0)}%</span>
        </div>
        <div className="detail-row">
          <span className="label">Reasoning:</span>
          <span className="value reasoning-text">{suggestion.reasoning}</span>
        </div>
      </div>
      
      <div className="suggestion-actions">
        <button 
          className="accept-btn" 
          onClick={() => onAction('accept')}
        >
          Accept Reorder
        </button>
        <button 
          className="reject-btn" 
          onClick={() => onAction('reject')}
        >
          Reject
        </button>
      </div>
    </div>
  );
};