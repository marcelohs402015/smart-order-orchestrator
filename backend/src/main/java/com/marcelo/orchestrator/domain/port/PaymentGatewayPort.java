package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayPort {

    PaymentResult processPayment(PaymentRequest request);

    PaymentResult refundPayment(String paymentId, BigDecimal amount);

    PaymentStatus checkPaymentStatus(String paymentId);
}

