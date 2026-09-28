package com.stockpulse.agent;

public class InventoryEvent {

    private Long productId;
    private Integer stockLevel;

    public InventoryEvent() {
    }

    public InventoryEvent(Long productId, Integer stockLevel) {
        this.productId = productId;
        this.stockLevel = stockLevel;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
}
