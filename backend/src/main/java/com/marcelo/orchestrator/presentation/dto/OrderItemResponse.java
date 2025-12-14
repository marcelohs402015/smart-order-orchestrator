package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de response para item de pedido.
 * 
 * <p>Representa um item de pedido retornado pela API REST.
 * Inclui informações calculadas como subtotal.</p>
 * 
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
 * @param productId ID do produto
 * @param productName Nome do produto
 * @param quantity Quantidade
 * @param unitPrice Preço unitário
 * @param subtotal Subtotal calculado (quantity * unitPrice)
 * 
 * @author Marcelo
 */
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
