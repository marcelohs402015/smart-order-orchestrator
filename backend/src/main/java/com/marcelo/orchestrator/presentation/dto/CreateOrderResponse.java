package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 * </ul>
 * 
     * @param success Indica se a saga foi executada com sucesso
     * @param order Pedido criado ou parcialmente processado (pode ser null em alguns cenários)
 * @param sagaExecutionId ID da execução da saga (para rastreamento)
 * @param errorMessage Mensagem de erro (null se sucesso)
 * 
 * @author Marcelo
 */
public record CreateOrderResponse(
    @JsonProperty("success")
    boolean success,
    
    @JsonProperty("order")
    OrderResponse order,
    
    @JsonProperty("sagaExecutionId")
    UUID sagaExecutionId,
    
    @JsonProperty("errorMessage")
    String errorMessage
) {
    /**
     * Cria response de sucesso.
     * 
     * @param order Pedido criado
     * @param sagaExecutionId ID da execução da saga
     * @return CreateOrderResponse com success=true
     */
    public static CreateOrderResponse success(OrderResponse order, UUID sagaExecutionId) {
        return new CreateOrderResponse(true, order, sagaExecutionId, null);
    }
    
    /**
     * Cria response de falha.
     * 
     * @param order Pedido associado à saga (pode ser null se falhou antes da criação)
     * @param sagaExecutionId ID da execução da saga
     * @param errorMessage Mensagem de erro
     * @return CreateOrderResponse com success=false
     */
    public static CreateOrderResponse failed(OrderResponse order, UUID sagaExecutionId, String errorMessage) {
        return new CreateOrderResponse(false, order, sagaExecutionId, errorMessage);
    }
    
    /**
     * Cria response indicando que saga está em progresso (idempotência ou estado intermediário).
     * 
     * <p>Padrão: Idempotência - usado quando uma saga com a mesma
     * idempotency_key já está sendo executada.</p>
     * 
     * @param order Pedido associado (pode ser null quando já existe saga em andamento)
     * @param sagaExecutionId ID da execução da saga em progresso
     * @param message Mensagem informativa
     * @return CreateOrderResponse com success=false e mensagem
     */
    public static CreateOrderResponse inProgress(OrderResponse order, UUID sagaExecutionId, String message) {
        return new CreateOrderResponse(false, order, sagaExecutionId, message);
    }
}
