package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;


public record OrderItemResponse(
    @JsonProperty("productId")
    UUID productId,
    
    @JsonProperty("productName")
    String productName,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("unitPrice")
    BigDecimal unitPrice,
    
    @JsonProperty("subtotal")
    BigDecimal subtotal
) {
}
