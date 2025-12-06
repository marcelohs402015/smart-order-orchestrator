package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Resultado da execução da saga.
 * 
 * <p>Contém o resultado final da saga, incluindo o pedido processado
 * e informações sobre o sucesso ou falha da execução.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderSagaResult {
    
    private final boolean success;
    private final Order order;
    private final UUID sagaExecutionId;
    private final String errorMessage;
    
    /**
     * Cria resultado de sucesso.
     */
    public static OrderSagaResult success(Order order, UUID sagaExecutionId) {
        return OrderSagaResult.builder()
            .success(true)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(null)
            .build();
    }
    
    /**
     * Cria resultado de falha.
     */
    public static OrderSagaResult failed(Order order, UUID sagaExecutionId, String errorMessage) {
        return OrderSagaResult.builder()
            .success(false)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(errorMessage)
            .build();
    }
}

