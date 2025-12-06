package com.marcelo.orchestrator.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de response para item de pedido.
 * 
 * <p>Representa um item de pedido retornado pela API REST.
 * Inclui informações calculadas como subtotal.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderItemResponse {
    
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

