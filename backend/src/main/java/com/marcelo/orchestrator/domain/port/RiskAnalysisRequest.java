package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public record RiskAnalysisRequest(
    UUID orderId,
    BigDecimal orderAmount,
    UUID customerId,
    String customerEmail,
    String paymentMethod,
    String additionalContext
) {

    public RiskAnalysisRequest {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than zero");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
    }
}

