package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
 * @param id ID do pedido
 * @param orderNumber Número do pedido (ex: ORD-1234567890)
 * @param status Status do pedido
 * @param customerId ID do cliente
 * @param customerName Nome do cliente
 * @param customerEmail Email do cliente
 * @param items Lista de itens do pedido
 * @param totalAmount Valor total do pedido
 * @param paymentId ID do pagamento no gateway externo
 * @param riskLevel Nível de risco após análise
 * @param createdAt Data e hora de criação
 * @param updatedAt Data e hora da última atualização
 * 
 * @author Marcelo
 */
public record OrderResponse(
    @JsonProperty("id")
    UUID id,
    
    @JsonProperty("orderNumber")
    String orderNumber,
    
    @JsonProperty("status")
    OrderStatus status,
    
    @JsonProperty("customerId")
    UUID customerId,
    
    @JsonProperty("customerName")
    String customerName,
    
    @JsonProperty("customerEmail")
    String customerEmail,
    
    @JsonProperty("items")
    List<OrderItemResponse> items,
    
    @JsonProperty("totalAmount")
    BigDecimal totalAmount,
    
    @JsonProperty("paymentId")
    String paymentId,
    
    @JsonProperty("riskLevel")
    RiskLevel riskLevel,
    
    @JsonProperty("createdAt")
    LocalDateTime createdAt,
    
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt
) {
    /**
     * Factory method para criar OrderResponse a partir de Order (domínio).
     * 
     * @param order Entidade de domínio
     * @return OrderResponse
     */
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemsResponse = order.getItems() != null
            ? order.getItems().stream()
                .map(item -> new OrderItemResponse(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
                ))
                .toList() // Java 16+ - mais conciso que Collectors.toList()
            : List.of();
        
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getCustomerId(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            itemsResponse,
            order.getTotalAmount(),
            order.getPaymentId(),
            order.getRiskLevel(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
