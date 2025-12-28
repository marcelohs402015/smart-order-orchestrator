package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.RiskLevel;

import java.time.LocalDateTime;

public record RiskAnalysisResult(
    RiskLevel riskLevel,
    Double confidenceScore,
    String reason,
    LocalDateTime analyzedAt
) {

    public boolean isLowRisk() {
        return riskLevel == RiskLevel.LOW;
    }

    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH;
    }
}

