import React, { useState } from 'react';
import { SuggestionSection } from './SuggestionSection';

const ProductCard = ({ product, onSimulateSale }) => {
  const [isProcessing, setIsProcessing] = useState(false);

  const handleSaleClick = async () => {
    if (isProcessing) return;
    
    setIsProcessing(true);
    await onSimulateSale(product.id, product.name);
    setIsProcessing(false);
  };

  const getStatusColor = () => {
    if (product.stockLevel < product.reorderThreshold) {
      return 'low-stock';
    }
    if (product.demandVelocity > 15) {
      return 'high-demand';
    }
    return 'normal';
  };

  // Function to handle product updates from suggestions
  const handleProductUpdate = () => {
    // This will trigger the parent to refresh the product data
    if (onSimulateSale) {
      onSimulateSale(product.id, product.name);
    }
  };

  return (
    <div className="product-card">
      <div className="product-header">
        <h3>{product.name}</h3>
        <span className={`status-badge ${getStatusColor()}`}>
          {product.stockLevel < product.reorderThreshold ? 'LOW STOCK' : 
           product.demandVelocity > 15 ? 'HIGH DEMAND' : 'NORMAL'}
        </span>
      </div>
      
      <div className="product-details">
        <div className="detail-row">
          <span className="label">SKU:</span>
          <span className="value">{product.sku}</span>
        </div>
        <div className="detail-row">
          <span className="label">Category:</span>
          <span className="value">{product.category}</span>
        </div>
        <div className="detail-row">
          <span className="label">Price:</span>
          <span className="value">${product.currentPrice?.toFixed(2) || '0.00'}</span>
        </div>
        <div className="detail-row">
          <span className="label">Stock:</span>
          <span className="value">{product.stockLevel}</span>
        </div>
        <div className="detail-row">
          <span className="label">Reorder Threshold:</span>
          <span className="value">{product.reorderThreshold}</span>
        </div>
        <div className="detail-row">
          <span className="label">Demand Velocity:</span>
          <span className="value">{product.demandVelocity?.toFixed(2) || '0.00'}</span>
        </div>
      </div>
      
      <button 
        className="simulate-sale-btn"
        onClick={handleSaleClick}
        disabled={isProcessing}
      >
        {isProcessing ? 'Processing...' : 'Simulate Sale'}
      </button>
      
      <SuggestionSection productId={product.id} onProductUpdate={handleProductUpdate} />
    </div>
  );
};

export default ProductCard;