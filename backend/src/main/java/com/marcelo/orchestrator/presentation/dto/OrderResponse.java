package com.marcelo.orchestrator.presentation.dto;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de response para pedido.
 * 
 * <p>Representa um pedido retornado pela API REST.
 * Converte entidade de domínio para formato adequado para exposição via HTTP.</p>
 * 
 * <h3>Por que DTO separado?</h3>
 * <ul>
 *   <li><strong>Segurança:</strong> Não expõe estrutura interna do domínio</li>
 *   <li><strong>Flexibilidade:</strong> Pode ter campos diferentes da entidade</li>
 *   <li><strong>Versionamento:</strong> Pode evoluir independentemente</li>
 *   <li><strong>Performance:</strong> Pode incluir apenas campos necessários</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderResponse {
    
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String paymentId;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method para criar OrderResponse a partir de Order (domínio).
     * 
     * @param order Entidade de domínio
     * @return OrderResponse
     */
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemsResponse = order.getItems() != null
            ? order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .build())
                .collect(Collectors.toList())
            : List.of();
        
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .customerId(order.getCustomerId())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .items(itemsResponse)
            .totalAmount(order.getTotalAmount())
            .paymentId(order.getPaymentId())
            .riskLevel(order.getRiskLevel())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}

