package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class OrderSagaResult {
    
    private final boolean success;
    private final boolean inProgress; // true se saga está em progresso (idempotência)
    private final Order order;
    private final UUID sagaExecutionId;
    private final String errorMessage;
    

    public static OrderSagaResult success(Order order, UUID sagaExecutionId) {
        return OrderSagaResult.builder()
            .success(true)
            .inProgress(false)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(null)
            .build();
    }
    
    public static OrderSagaResult failed(Order order, UUID sagaExecutionId, String errorMessage) {
        return OrderSagaResult.builder()
            .success(false)
            .inProgress(false)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(errorMessage)
            .build();
    }
    
    public static OrderSagaResult inProgress(UUID sagaExecutionId, Order order) {
        return OrderSagaResult.builder()
            .success(false)
            .inProgress(true)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage("Saga is already in progress")
            .build();
    }

    public static OrderSagaResult inProgress(UUID sagaExecutionId) {
        return inProgress(sagaExecutionId, null);
    }
}

