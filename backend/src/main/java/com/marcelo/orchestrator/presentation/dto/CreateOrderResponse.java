package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;


public record CreateOrderResponse(
    @JsonProperty("success")
    boolean success,
    
    @JsonProperty("inProgress")
    boolean inProgress,
    
    @JsonProperty("order")
    OrderResponse order,
    
    @JsonProperty("sagaExecutionId")
    UUID sagaExecutionId,
    
    @JsonProperty("errorMessage")
    String errorMessage
) {
    
    public static CreateOrderResponse success(OrderResponse order, UUID sagaExecutionId) {
        return new CreateOrderResponse(true, false, order, sagaExecutionId, null);
    }
    
    
    public static CreateOrderResponse failed(OrderResponse order, UUID sagaExecutionId, String errorMessage) {
        return new CreateOrderResponse(false, false, order, sagaExecutionId, errorMessage);
    }
    
    
    public static CreateOrderResponse inProgress(OrderResponse order, UUID sagaExecutionId, String message) {
        return new CreateOrderResponse(false, true, order, sagaExecutionId, message);
    }
}
