package com.marcelo.orchestrator.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request para item de pedido.
 * 
 * <p>Representa um item de pedido recebido via REST API.
 * Validações garantem que dados estão corretos antes de processar.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
@Jacksonized
public class OrderItemRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Product name is required")
    private String productName;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
}

