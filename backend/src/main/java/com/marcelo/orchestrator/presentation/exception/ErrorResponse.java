package com.marcelo.orchestrator.presentation.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;


public record ErrorResponse(
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @JsonProperty("status")
    int status,
    
    @JsonProperty("error")
    String error,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("details")
    Map<String, String> details 
) {
}
