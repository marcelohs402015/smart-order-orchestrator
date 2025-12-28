package com.marcelo.orchestrator.domain.port;

import java.util.UUID;


public interface RiskAnalysisPort {
    
    
    RiskAnalysisResult analyzeRisk(RiskAnalysisRequest request);
}

