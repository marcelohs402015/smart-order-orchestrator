package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResult(
    String paymentId,
    PaymentStatus status,
    String message,
    BigDecimal amount,
    LocalDateTime processedAt
) {

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
    
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
}

