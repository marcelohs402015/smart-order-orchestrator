package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


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
                .toList() 
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
