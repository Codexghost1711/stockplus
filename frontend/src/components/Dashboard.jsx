import React, { useState, useEffect } from 'react';
import { stockpulseApi } from '../api/stockpulseApi';
import ProductCard from './ProductCard';

const Dashboard = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch products on component mount
  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await stockpulseApi.getProducts();
      if (response.success) {
        setProducts(response.data);
      } else {
        setError(response.message || 'Failed to fetch products');
      }
    } catch (err) {
      setError('Failed to connect to backend');
    } finally {
      setLoading(false);
    }
  };

  const handleSimulateSale = async (productId, productName) => {
    try {
      // Simulate a sale of 1 unit
      const response = await stockpulseApi.simulateOrder(productId, 1);
      if (response.success) {
        // Refresh products to show updated stock
        fetchProducts();
        
        // Show success message
        alert(`Sale simulated for ${productName}`);
      } else {
        alert(`Failed to simulate sale: ${response.message}`);
      }
    } catch (err) {
      alert('Failed to connect to backend');
    }
  };

  if (loading) {
    return (
      <div className="dashboard">
        <header>
          <h1>StockPulse</h1>
          <p>AI Inventory & Dynamic Pricing</p>
        </header>
        <div className="loading">Loading products...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard">
        <header>
          <h1>StockPulse</h1>
          <p>AI Inventory & Dynamic Pricing</p>
        </header>
        <div className="error">Error: {error}</div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <header>
        <h1>StockPulse</h1>
        <p>AI Inventory & Dynamic Pricing</p>
      </header>
      
      <div className="products-grid">
        {products.map((product) => (
          <ProductCard 
            key={product.id} 
            product={product} 
            onSimulateSale={handleSimulateSale}
          />
        ))}
      </div>
    </div>
  );
};

export default Dashboard;