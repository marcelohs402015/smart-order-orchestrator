package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;


public record CreateOrderRequest(
    @NotNull(message = "Customer ID is required")
    @JsonProperty("customerId")
    UUID customerId,
    
    @NotBlank(message = "Customer name is required")
    @JsonProperty("customerName")
    String customerName,
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @JsonProperty("customerEmail")
    String customerEmail,
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @JsonProperty("items")
    List<OrderItemRequest> items,
    
    @NotBlank(message = "Payment method is required")
    @JsonProperty("paymentMethod")
    String paymentMethod,
    
    @JsonProperty("currency")
    String currency, 
    
    
    @JsonProperty("idempotencyKey")
    String idempotencyKey 
) {
}
