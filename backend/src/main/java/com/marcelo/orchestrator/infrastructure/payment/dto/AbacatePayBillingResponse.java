package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


public record AbacatePayBillingResponse(
    @JsonProperty("data")
    AbacatePayBillingData data,
    
    @JsonProperty("error")
    String error
) {
    
    public boolean isSuccess() {
        return data != null && error == null;
    }
    
    
    public record AbacatePayBillingData(
        @JsonProperty("id")
        String id,
        
        @JsonProperty("url")
        String url,
        
        @JsonProperty("amount")
        Integer amount,
        
        @JsonProperty("status")
        String status,
        
        @JsonProperty("devMode")
        Boolean devMode,
        
        @JsonProperty("methods")
        String[] methods,
        
        @JsonProperty("frequency")
        String frequency,
        
        @JsonProperty("customer")
        AbacatePayCustomerData customer,
        
        @JsonProperty("createdAt")
        LocalDateTime createdAt,
        
        @JsonProperty("updatedAt")
        LocalDateTime updatedAt
    ) {}
    
    
    public record AbacatePayCustomerData(
        @JsonProperty("id")
        String id,
        
        @JsonProperty("metadata")
        AbacatePayCustomerMetadata metadata
    ) {}
    
    
    public record AbacatePayCustomerMetadata(
        @JsonProperty("name")
        String name,
        
        @JsonProperty("cellphone")
        String cellphone,
        
        @JsonProperty("email")
        String email,
        
        @JsonProperty("taxId")
        String taxId
    ) {}
}
