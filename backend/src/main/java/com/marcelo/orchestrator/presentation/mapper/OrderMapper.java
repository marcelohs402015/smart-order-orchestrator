package com.marcelo.orchestrator.presentation.mapper;

import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.presentation.dto.OrderItemRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper entre DTOs de apresentação e objetos de domínio.
 * 
 * <p>Converte DTOs de request/response para objetos de domínio e vice-versa.
 * Mantém separação entre camada de apresentação e domínio.</p>
 * 
 * @author Marcelo
 */
public class OrderMapper {
    
    /**
     * Converte OrderItemRequest para OrderItem (domínio).
     */
    public static OrderItem toDomain(OrderItemRequest request) {
        return OrderItem.builder()
            .productId(request.getProductId())
            .productName(request.getProductName())
            .quantity(request.getQuantity())
            .unitPrice(request.getUnitPrice())
            .build();
    }
    
    /**
     * Converte lista de OrderItemRequest para lista de OrderItem (domínio).
     */
    public static List<OrderItem> toDomainList(List<OrderItemRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
            .map(OrderMapper::toDomain)
            .collect(Collectors.toList());
    }
}

