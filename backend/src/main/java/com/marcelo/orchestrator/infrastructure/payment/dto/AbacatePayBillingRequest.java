package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
public class AbacatePayBillingRequest {
    
    
    @JsonProperty("methods")
    private String[] methods;
    
    
    @JsonProperty("frequency")
    private String frequency;
    
    
    @JsonProperty("products")
    private AbacatePayProductRequest[] products;
    
    
    @JsonProperty("returnUrl")
    private String returnUrl;
    
    
    @JsonProperty("completionUrl")
    private String completionUrl;
    
    
    @JsonProperty("customer")
    private AbacatePayCustomerRequest customer;
    
    
    public static Integer toCents(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }
}

