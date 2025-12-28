package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderItem {
    
    private final UUID productId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    
    public BigDecimal getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
