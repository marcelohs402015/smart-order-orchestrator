package com.marcelo.orchestrator.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
 * <h3>Por que Record?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Dados não podem ser alterados após criação</li>
 *   <li><strong>Simplicidade:</strong> Menos código, mais legível (Java 17+)</li>
 *   <li><strong>Performance:</strong> Menos overhead que classes tradicionais</li>
 *   <li><strong>Thread-Safe:</strong> Imutabilidade garante segurança em concorrência</li>
 * </ul>
 * 
 * @param customerId ID do cliente (obrigatório)
 * @param customerName Nome do cliente (obrigatório)
 * @param customerEmail Email do cliente (obrigatório, deve ser válido)
 * @param items Lista de itens do pedido (obrigatória, não vazia)
 * @param paymentMethod Método de pagamento (obrigatório)
 * @param currency Moeda (opcional, padrão: BRL)
 * @param idempotencyKey Chave de idempotência (opcional - será gerado se não fornecido)
 * 
 * @author Marcelo
 */
public record CreateOrderRequest(
    @NotNull(message = "Customer ID is required")
    @JsonProperty("customerId")
    UUID customerId,
    
    @NotBlank(message = "Customer name is required")
    @JsonProperty("customerName")
    String customerName,
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @JsonProperty("customerEmail")
    String customerEmail,
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @JsonProperty("items")
    List<OrderItemRequest> items,
    
    @NotBlank(message = "Payment method is required")
    @JsonProperty("paymentMethod")
    String paymentMethod,
    
    @JsonProperty("currency")
    String currency, // Opcional, padrão: BRL
    
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
    @JsonProperty("idempotencyKey")
    String idempotencyKey // Opcional - será gerado se não fornecido
) {
}
