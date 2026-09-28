package com.stockpulse.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of LLMGateway for LiteLLM/qwen-cursor provider.
 * Uses the specific endpoint and authentication from the provided curl command.
 */
@Component
public class LiteLLMGateway implements LLMGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(LiteLLMGateway.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${stockpulse.ai.base-url:https://litellm-qc.zycus.net/v1}")
    private String baseUrl;
    
    @Value("${stockpulse.ai.api-key:}")
    private String apiKey;
    
    @Value("${stockpulse.ai.model:qwen-cursor}")
    private String model;
    
    @Value("${stockpulse.ai.product:PC1}")
    private String product;
    
    @Value("${stockpulse.ai.cookie:}")
    private String cookie;
    
    private final RestClient restClient;
    
    public LiteLLMGateway() {
        this.restClient = RestClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    logger.warn("HTTP error {}: {}", response.getStatusCode(), response.getStatusText());
                })
                .build();
    }
    
    @Override
    public LLMResponse callLLM(String prompt) {
        try {
            logger.debug("Calling LiteLLM with prompt: {}", prompt);
            
            // Prepare the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            // Create messages array
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", new Map[]{message});
            
            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }
            headers.set("product", product);
            
            if (cookie != null && !cookie.isEmpty()) {
                headers.set("Cookie", cookie);
            }
            
            // Make the HTTP request
            String url = baseUrl + "/chat/completions";
            logger.debug("Making request to: {}", url);
            
            String responseBody = restClient.post()
                    .uri(url)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            
            // Parse the response
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode choices = responseJson.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode messageNode = firstChoice.get("message");
                
                if (messageNode != null) {
                    String content = messageNode.get("content").asText();
                    logger.debug("Received response from LLM: {}", content);
                    return new LLMResponse(content, "LiteLLM", model);
                }
            }
            
            logger.warn("Unexpected response format from LLM: {}", responseBody);
            return new LLMResponse("Unable to parse response", "LiteLLM", model);
            
        } catch (RestClientResponseException e) {
            logger.error("HTTP error calling LLM: {}", e.getStatusCode(), e);
            return new LLMResponse("HTTP error: " + e.getStatusCode(), "LiteLLM", model);
        } catch (Exception e) {
            logger.error("Error calling LLM: {}", e.getMessage(), e);
            return new LLMResponse("Error: " + e.getMessage(), "LiteLLM", model);
        }
    }
}