package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.Order;

/**
 * Porta (Port) para envio de notificações.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) para comunicação
 * com sistemas de notificação (email, SMS, webhook, etc.).</p>
 * 
 * <h3>Arquitetura Hexagonal:</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na Infrastructure (ex: EmailService, WebhookService)</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes de SMTP, APIs de SMS, etc.</li>
 * </ul>
 * 
 * <h3>Por que Interface ao invés de Classe Concreta?</h3>
 * <ul>
 *   <li><strong>Dependency Inversion Principle (SOLID):</strong> Domínio não depende de implementação</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar para testes unitários</li>
 *   <li><strong>Flexibilidade:</strong> Trocar provedor de notificação sem alterar domínio</li>
 *   <li><strong>Múltiplas Implementações:</strong> Pode ter adaptadores para email, SMS, webhook, etc.</li>
 * </ul>
 * 
 * <h3>Tipos de Notificação:</h3>
 * <ul>
 *   <li><strong>Order Created:</strong> Notifica cliente quando pedido é criado</li>
 *   <li><strong>Status Changed:</strong> Notifica quando status do pedido muda</li>
 *   <li><strong>Payment Failed:</strong> Notifica sobre falha de pagamento</li>
 *   <li><strong>Order Cancelled:</strong> Notifica sobre cancelamento</li>
 * </ul>
 * 
 * @author Marcelo
 */
public interface NotificationPort {
    
    /**
     * Notifica sobre criação de um novo pedido.
     * 
     * <p>Envia notificação ao cliente informando que o pedido foi criado com sucesso.
     * A implementação pode enviar email, SMS, push notification, etc.</p>
     * 
     * @param order Pedido criado
     */
    void notifyOrderCreated(Order order);
    
    /**
     * Notifica sobre mudança de status do pedido.
     * 
     * <p>Envia notificação quando o status do pedido muda (ex: PENDING → PAID).
     * Útil para manter cliente informado sobre progresso do pedido.</p>
     * 
     * @param order Pedido com status atualizado
     */
    void notifyOrderStatusChanged(Order order);
    
    /**
     * Notifica sobre falha no pagamento.
     * 
     * <p>Envia notificação quando pagamento falha, permitindo que cliente
     * tente novamente ou escolha outro método de pagamento.</p>
     * 
     * @param order Pedido com pagamento falhado
     */
    void notifyPaymentFailed(Order order);
    
    /**
     * Notifica sobre cancelamento do pedido.
     * 
     * <p>Envia notificação quando pedido é cancelado (pelo cliente ou sistema).
     * Pode incluir informações sobre reembolso se aplicável.</p>
     * 
     * @param order Pedido cancelado
     */
    void notifyOrderCancelled(Order order);
}

