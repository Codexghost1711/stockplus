package com.stockpulse.ai;

public class LLMResponse {

    private String rawResponse;
    private String provider;
    private String model;

    public LLMResponse() {
    }

    public LLMResponse(String rawResponse, String provider, String model) {
        this.rawResponse = rawResponse;
        this.provider = provider;
        this.model = model;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
