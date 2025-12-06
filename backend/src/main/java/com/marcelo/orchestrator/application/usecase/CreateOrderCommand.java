package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * Command (DTO de entrada) para criação de pedido.
 * 
 * <p>Implementa o padrão <strong>Command</strong> do CQRS (Command Query Responsibility Segregation).
 * Commands representam intenções de mudança de estado no sistema.</p>
 * 
 * <h3>Por que Command separado da entidade?</h3>
 * <ul>
 *   <li><strong>Separação de Concerns:</strong> Dados de entrada vs. modelo de domínio</li>
 *   <li><strong>Validação:</strong> Validações de entrada diferentes de regras de negócio</li>
 *   <li><strong>Flexibilidade:</strong> Command pode ter campos diferentes da entidade</li>
 *   <li><strong>Segurança:</strong> Não expõe entidade de domínio diretamente</li>
 * </ul>
 * 
 * <h3>Uso:</h3>
 * <p>Este command é recebido pela camada Presentation (REST Controller),
 * validado, e então passado para o Use Case que cria a entidade de domínio.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class CreateOrderCommand {
    
    /**
     * ID do cliente que está fazendo o pedido.
     */
    private final UUID customerId;
    
    /**
     * Nome do cliente (snapshot no momento do pedido).
     */
    private final String customerName;
    
    /**
     * Email do cliente (para notificações).
     */
    private final String customerEmail;
    
    /**
     * Lista de itens do pedido.
     * Cada item contém produto, quantidade e preço unitário.
     */
    private final List<OrderItem> items;
}

