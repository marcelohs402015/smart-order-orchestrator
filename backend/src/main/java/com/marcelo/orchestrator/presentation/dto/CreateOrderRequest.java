package com.marcelo.orchestrator.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * DTO de request para criação de pedido via Saga.
 * 
 * <p>Encapsula todos os dados necessários para executar a saga completa:
 * criação do pedido, processamento de pagamento e análise de risco.</p>
 * 
 * <h3>Validações:</h3>
 * <ul>
 *   <li>Customer ID obrigatório</li>
 *   <li>Email válido</li>
 *   <li>Lista de itens não vazia</li>
 *   <li>Método de pagamento obrigatório</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
@Jacksonized
public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String currency; // Opcional, padrão: BRL
    
    /**
     * Chave de idempotência para prevenir execuções duplicadas.
     * 
     * <p>Padrão: Idempotency Key - garante que requisições duplicadas
     * (por timeout, retry, ou usuário clicando várias vezes) não criem
     * pedidos duplicados.</p>
     * 
     * <p>Opcional: Se não fornecido, será gerado automaticamente.
     * Recomendado: Cliente deve fornecer UUID único por requisição.</p>
     */
    private String idempotencyKey; // Opcional - será gerado se não fornecido
}

