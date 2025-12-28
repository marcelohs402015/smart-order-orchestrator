package com.marcelo.orchestrator.application.usecase;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProcessPaymentCommand {
    
    private final UUID orderId;
    private final String currency;
    private final String paymentMethod;
}

