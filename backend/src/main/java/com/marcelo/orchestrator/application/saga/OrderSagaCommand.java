package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.CreateOrderCommand;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentCommand;
import com.marcelo.orchestrator.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * Command para execução completa da saga de pedido.
 * 
 * <p>Encapsula todos os dados necessários para executar os 3 passos da saga:
 * 1. Criar pedido
 * 2. Processar pagamento
 * 3. Analisar risco</p>
 * 
 * <h3>Por que um Command único?</h3>
 * <p>O Saga Orchestrator precisa de todos os dados de uma vez para orquestrar
 * os passos sequencialmente. Isso simplifica a interface e garante que todos
 * os dados necessários estejam disponíveis.</p>
 * 
 * <h3>Padrão: Idempotência</h3>
 * <p>O campo {@code idempotencyKey} permite garantir que executar a mesma saga
 * múltiplas vezes produza o mesmo resultado. Se uma saga com a mesma chave já
 * foi executada, retorna o resultado anterior ao invés de criar novo pedido.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderSagaCommand {
    
    /**
     * Chave de idempotência para prevenir execuções duplicadas.
     * 
     * <p>Padrão: Idempotency Key - garante que requisições duplicadas
     * (por timeout, retry, ou usuário clicando várias vezes) não criem
     * pedidos duplicados.</p>
     * 
     * <p>Deve ser gerado pelo cliente e ser único por requisição.
     * Recomendado: UUID ou hash dos dados da requisição.</p>
     */
    private final String idempotencyKey;
    
    // Dados para criação do pedido
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final List<OrderItem> items;
    
    // Dados para processamento de pagamento
    private final String paymentMethod;
    private final String currency;
    
    /**
     * Converte para CreateOrderCommand.
     */
    public CreateOrderCommand toCreateOrderCommand() {
        return CreateOrderCommand.builder()
            .customerId(customerId)
            .customerName(customerName)
            .customerEmail(customerEmail)
            .items(items)
            .build();
    }
    
    /**
     * Converte para ProcessPaymentCommand.
     * 
     * @param orderId ID do pedido criado no Step 1
     */
    public ProcessPaymentCommand toProcessPaymentCommand(UUID orderId) {
        return ProcessPaymentCommand.builder()
            .orderId(orderId)
            .paymentMethod(paymentMethod)
            .currency(currency != null ? currency : "BRL")
            .build();
    }
    
    /**
     * Converte para AnalyzeRiskCommand.
     * 
     * @param orderId ID do pedido (já pago no Step 2)
     */
    public AnalyzeRiskCommand toAnalyzeRiskCommand(UUID orderId) {
        return AnalyzeRiskCommand.builder()
            .orderId(orderId)
            .paymentMethod(paymentMethod)
            .build();
    }
}

