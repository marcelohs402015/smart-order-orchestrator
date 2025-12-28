package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;


public record OrderItemRequest(
    @NotNull(message = "Product ID is required")
    @JsonProperty("productId")
    UUID productId,
    
    @NotNull(message = "Product name is required")
    @JsonProperty("productName")
    String productName,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    Integer quantity,
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @JsonProperty("unitPrice")
    BigDecimal unitPrice
) {
}
