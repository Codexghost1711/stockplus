// API service for StockPulse backend
const API_BASE_URL = 'http://localhost:8080';

export const stockpulseApi = {
  // Products
  getProducts: async () => {
    const response = await fetch(`${API_BASE_URL}/products`);
    return response.json();
  },

  simulateOrder: async (productId, quantity) => {
    const response = await fetch(`${API_BASE_URL}/products/${productId}/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ quantity }),
    });
    return response.json();
  },

  // Suggestions
  getPricingSuggestions: async (productId) => {
    const response = await fetch(`${API_BASE_URL}/products/${productId}/pricing-suggestions`);
    return response.json();
  },

  getReorderSuggestions: async (productId) => {
    const response = await fetch(`${API_BASE_URL}/products/${productId}/reorder-suggestions`);
    return response.json();
  },

  updatePricingSuggestionStatus: async (suggestionId, status) => {
    const response = await fetch(`${API_BASE_URL}/products/pricing-suggestions/${suggestionId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status }),
    });
    return response.json();
  },

  updateReorderSuggestionStatus: async (suggestionId, status) => {
    const response = await fetch(`${API_BASE_URL}/products/reorder-suggestions/${suggestionId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status }),
    });
    return response.json();
  },
};