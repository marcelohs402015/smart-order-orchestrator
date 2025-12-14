package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request para item de pedido.
 * 
 * <p>Representa um item de pedido recebido via REST API.
 * Validações garantem que dados estão corretos antes de processar.</p>
 * 
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
 * @param productId ID do produto (obrigatório)
 * @param productName Nome do produto (obrigatório)
 * @param quantity Quantidade (obrigatória, mínimo 1)
 * @param unitPrice Preço unitário (obrigatório, deve ser positivo)
 * 
 * @author Marcelo
 */
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
