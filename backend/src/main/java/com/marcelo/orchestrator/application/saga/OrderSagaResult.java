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
 * <h3>Padrão: Idempotência</h3>
 * <p>O método {@code inProgress()} é usado quando uma saga com a mesma
 * idempotency_key já está em execução, permitindo que o cliente saiba
 * que a requisição foi recebida e está sendo processada.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderSagaResult {
    
    private final boolean success;
    private final boolean inProgress; // true se saga está em progresso (idempotência)
    private final Order order;
    private final UUID sagaExecutionId;
    private final String errorMessage;
    
    /**
     * Cria resultado de sucesso.
     */
    public static OrderSagaResult success(Order order, UUID sagaExecutionId) {
        return OrderSagaResult.builder()
            .success(true)
            .inProgress(false)
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
            .inProgress(false)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage(errorMessage)
            .build();
    }
    
    /**
     * Cria resultado indicando que saga está em progresso (idempotência ou estado intermediário).
     *
     * <p>Usado quando uma saga com a mesma idempotency_key já está sendo executada
     * ou quando o fluxo está em um estado intermediário (por exemplo, pagamento pendente).</p>
     */
    public static OrderSagaResult inProgress(UUID sagaExecutionId, Order order) {
        return OrderSagaResult.builder()
            .success(false)
            .inProgress(true)
            .order(order)
            .sagaExecutionId(sagaExecutionId)
            .errorMessage("Saga is already in progress")
            .build();
    }
    
    /**
     * Sobrecarga para manter compatibilidade com chamadas antigas que não passam Order.
     */
    public static OrderSagaResult inProgress(UUID sagaExecutionId) {
        return inProgress(sagaExecutionId, null);
    }
}

