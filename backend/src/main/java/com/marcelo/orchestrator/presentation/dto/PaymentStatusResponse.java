package com.marcelo.orchestrator.presentation.dto;

import com.marcelo.orchestrator.domain.port.PaymentStatus;

/**
 * DTO for exposing payment status via REST API.
 *
 * @param paymentId ID of the payment in the external gateway
 * @param status Mapped domain payment status
 */
public record PaymentStatusResponse(
    String paymentId,
    PaymentStatus status
) {
}


