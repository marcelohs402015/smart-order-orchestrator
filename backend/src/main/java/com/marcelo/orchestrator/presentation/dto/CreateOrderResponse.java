package com.marcelo.orchestrator.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO de response para criação de pedido via Saga.
 * 
 * <p>Retorna resultado da execução da saga, incluindo:
 * - Status de sucesso/falha
 * - Pedido criado (se sucesso)
 * - ID da execução da saga (para rastreamento)
 * - Mensagem de erro (se falhou)</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class CreateOrderResponse {
    
    private boolean success;
    private OrderResponse order;
    private UUID sagaExecutionId;
    private String errorMessage;
    
    /**
     * Cria response de sucesso.
     */
    public static CreateOrderResponse success(OrderResponse order, UUID sagaExecutionId) {
        return CreateOrderResponse.builder()
            .success(true)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(null)
            .build();
    }
    
    /**
     * Cria response de falha.
     */
    public static CreateOrderResponse failed(UUID sagaExecutionId, String errorMessage) {
        return CreateOrderResponse.builder()
            .success(false)
            .order(null)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(errorMessage)
            .build();
    }
    
    /**
     * Cria response indicando que saga está em progresso (idempotência).
     * 
     * <p>Padrão: Idempotência - usado quando uma saga com a mesma
     * idempotency_key já está sendo executada.</p>
     */
    public static CreateOrderResponse inProgress(UUID sagaExecutionId, String message) {
        return CreateOrderResponse.builder()
            .success(false)
            .order(null)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(message)
            .build();
    }
}

