package com.marcelo.orchestrator.presentation.mapper;

import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.presentation.dto.OrderItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class OrderPresentationMapper {
    
    
    public OrderItem toDomain(OrderItemRequest request) {
        if (request == null) {
            log.warn("Attempted to map null OrderItemRequest to domain");
            throw new IllegalArgumentException("OrderItemRequest cannot be null");
        }
        
        log.debug("Mapping OrderItemRequest to domain: productId={}, productName={}", 
            request.productId(), request.productName());
        
        return OrderItem.builder()
            .productId(request.productId())
            .productName(request.productName())
            .quantity(request.quantity())
            .unitPrice(request.unitPrice())
            .build();
    }
    
    
    public List<OrderItem> toDomainList(List<OrderItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.debug("Mapping empty or null list of OrderItemRequest to domain");
            return List.of();
        }
        
        log.debug("Mapping {} OrderItemRequest(s) to domain", requests.size());
        
        return requests.stream()
            .map(this::toDomain)
            .toList(); 
    }
}

