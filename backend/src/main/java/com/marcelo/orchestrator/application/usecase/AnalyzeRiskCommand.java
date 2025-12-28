package com.marcelo.orchestrator.application.usecase;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AnalyzeRiskCommand {
    
    private final UUID orderId;
    private final String paymentMethod;
}

