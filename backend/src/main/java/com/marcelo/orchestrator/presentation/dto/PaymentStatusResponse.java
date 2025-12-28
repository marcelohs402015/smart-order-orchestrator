package com.marcelo.orchestrator.presentation.dto;

import com.marcelo.orchestrator.domain.port.PaymentStatus;


public record PaymentStatusResponse(
    String paymentId,
    PaymentStatus status
) {
}


