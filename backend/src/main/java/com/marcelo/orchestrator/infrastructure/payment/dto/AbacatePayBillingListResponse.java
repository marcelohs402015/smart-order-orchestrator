package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record AbacatePayBillingListResponse(
    @JsonProperty("data")
    List<AbacatePayBillingResponse.AbacatePayBillingData> data,
    
    @JsonProperty("error")
    String error
) {
    
    public boolean isSuccess() {
        return data != null && error == null;
    }
}

