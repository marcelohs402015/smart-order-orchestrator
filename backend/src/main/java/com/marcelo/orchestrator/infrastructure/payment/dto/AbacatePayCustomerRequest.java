package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class AbacatePayCustomerRequest {
    
    
    @JsonProperty("name")
    private String name;
    
    
    @JsonProperty("cellphone")
    private String cellphone;
    
    
    @JsonProperty("email")
    private String email;
    
    
    @JsonProperty("taxId")
    private String taxId;
}

