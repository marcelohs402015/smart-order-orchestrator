package com.marcelo.orchestrator.application.saga;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.CreateOrderCommand;
import com.marcelo.orchestrator.application.usecase.ProcessPaymentCommand;
import com.marcelo.orchestrator.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderSagaCommand {
    
    private final String idempotencyKey;
    
    // Dados para criação do pedido
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final List<OrderItem> items;
    
    // Dados para processamento de pagamento
    private final String paymentMethod;
    private final String currency;
    

    public CreateOrderCommand toCreateOrderCommand() {
        return CreateOrderCommand.builder()
            .customerId(customerId)
            .customerName(customerName)
            .customerEmail(customerEmail)
            .items(items)
            .build();
    }
    
    public ProcessPaymentCommand toProcessPaymentCommand(UUID orderId) {
        return ProcessPaymentCommand.builder()
            .orderId(orderId)
            .paymentMethod(paymentMethod)
            .currency(currency != null ? currency : "BRL")
            .build();
    }
    
    public AnalyzeRiskCommand toAnalyzeRiskCommand(UUID orderId) {
        return AnalyzeRiskCommand.builder()
            .orderId(orderId)
            .paymentMethod(paymentMethod)
            .build();
    }
}

