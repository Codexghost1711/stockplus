package com.stockpulse.ai;

public interface LLMGateway {

    LLMResponse callLLM(String prompt);
}
