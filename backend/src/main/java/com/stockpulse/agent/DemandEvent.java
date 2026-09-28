package com.stockpulse.agent;

public class DemandEvent {

    private Long productId;
    private Double demandVelocity;

    public DemandEvent() {
    }

    public DemandEvent(Long productId, Double demandVelocity) {
        this.productId = productId;
        this.demandVelocity = demandVelocity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getDemandVelocity() {
        return demandVelocity;
    }

    public void setDemandVelocity(Double demandVelocity) {
        this.demandVelocity = demandVelocity;
    }
}
