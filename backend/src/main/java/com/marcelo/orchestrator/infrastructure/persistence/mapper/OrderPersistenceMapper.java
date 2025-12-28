package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class OrderPersistenceMapper {
    
    
    public OrderEntity toEntity(Order order) {
        if (order == null) {
            log.warn("Attempted to map null Order to entity");
            return null;
        }
        
        log.debug("Mapping Order to entity: id={}, orderNumber={}", order.getId(), order.getOrderNumber());
        
        OrderEntity entity = OrderEntity.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .customerId(order.getCustomerId())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .totalAmount(order.getTotalAmount())
            .paymentId(order.getPaymentId())
            .riskLevel(order.getRiskLevel())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            
            
            .build();
        
        
        mapItemsAfterMapping(order, entity);
        
        return entity;
    }
    
    
    private void mapItemsAfterMapping(Order order, OrderEntity entity) {
        
        if (entity.getItems() == null) {
            entity.setItems(new ArrayList<>());
        }
        
        if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
            
            
            if (!entity.getItems().isEmpty()) {
                entity.getItems().clear();
            }
            
            
            List<OrderItemEntity> newItems = mapItemsToEntity(order.getItems(), entity);
            entity.getItems().addAll(newItems);
        }
        
    }
    
    
    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            log.warn("Attempted to map null OrderEntity to domain");
            return null;
        }
        
        log.debug("Mapping OrderEntity to domain: id={}, orderNumber={}", entity.getId(), entity.getOrderNumber());
        
        List<OrderItem> items = entity.getItems() != null 
            ? mapItemsToDomain(entity.getItems())
            : List.of();
        
        return Order.builder()
            .id(entity.getId())
            .orderNumber(entity.getOrderNumber())
            .status(entity.getStatus())
            .customerId(entity.getCustomerId())
            .customerName(entity.getCustomerName())
            .customerEmail(entity.getCustomerEmail())
            .items(items)
            .totalAmount(entity.getTotalAmount())
            .paymentId(entity.getPaymentId())
            .riskLevel(entity.getRiskLevel())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    
    public void updateEntity(Order order, OrderEntity entity) {
        if (order == null || entity == null) {
            log.warn("Attempted to update entity with null order or entity");
            return;
        }
        
        log.debug("Updating OrderEntity: id={}, orderNumber={}", entity.getId(), entity.getOrderNumber());
        
        
        entity.setOrderNumber(order.getOrderNumber());
        entity.setStatus(order.getStatus());
        entity.setCustomerId(order.getCustomerId());
        entity.setCustomerName(order.getCustomerName());
        entity.setCustomerEmail(order.getCustomerEmail());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setPaymentId(order.getPaymentId());
        entity.setRiskLevel(order.getRiskLevel());
        entity.setUpdatedAt(order.getUpdatedAt());
        
        
        
    }
    
    
    public List<OrderItemEntity> mapItemsToEntity(List<OrderItem> items, OrderEntity order) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        
        log.debug("Mapping {} OrderItem(s) to entity", items.size());
        
        return items.stream()
            .map(item -> OrderItemEntity.builder()
                
                
                .order(order)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(Collectors.toList());
    }
    
    
    public List<OrderItem> mapItemsToDomain(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        
        log.debug("Mapping {} OrderItemEntity(s) to domain", items.size());
        
        return items.stream()
            .map(item -> OrderItem.builder()
                
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(Collectors.toList());
    }
}

