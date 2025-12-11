package com.marcelo.orchestrator.infrastructure.notification.adapter;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter de notificação que apenas loga (implementação simples para desenvolvimento).
 * 
 * <p>Implementa o padrão <strong>Adapter Pattern</strong> da Hexagonal Architecture,
 * fornecendo uma implementação de NotificationPort que registra notificações em logs
 * ao invés de enviar emails, SMS ou webhooks reais.</p>
 * 
 * <h3>Padrão: Adapter Pattern</h3>
 * <ul>
 *   <li><strong>Adapter:</strong> Esta classe - adapta a interface NotificationPort</li>
 *   <li><strong>Target:</strong> NotificationPort (interface do domínio)</li>
 *   <li><strong>Adaptee:</strong> Logger (simula serviço de notificação)</li>
 * </ul>
 * 
 * <h3>Por que Logging Adapter?</h3>
 * <ul>
 *   <li><strong>Desenvolvimento:</strong> Não precisa configurar SMTP, APIs de SMS, etc.</li>
 *   <li><strong>Simplicidade:</strong> Para ambientes que não precisam de notificações reais</li>
 *   <li><strong>Observabilidade:</strong> Logs permitem verificar que notificações foram "enviadas"</li>
 *   <li><strong>Testabilidade:</strong> Fácil verificar nos logs se notificações foram chamadas</li>
 * </ul>
 * 
 * <h3>Limitações:</h3>
 * <ul>
 *   <li><strong>Não Envia Notificações Reais:</strong> Apenas loga</li>
 *   <li><strong>Não Persistente:</strong> Logs podem ser perdidos</li>
 * </ul>
 * 
 * <h3>Para Produção:</h3>
 * <p>Crie implementações específicas:
 * <ul>
 *   <li>EmailNotificationAdapter - envia emails via SMTP/SendGrid</li>
 *   <li>SmsNotificationAdapter - envia SMS via Twilio</li>
 *   <li>WebhookNotificationAdapter - envia webhooks HTTP</li>
 * </ul>
 * </p>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
public class LoggingNotificationAdapter implements NotificationPort {
    
    @Override
    public void notifyOrderCreated(Order order) {
        log.info("📧 [NOTIFICATION] Order created - Order ID: {}, Number: {}, Customer: {} ({})", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerName(),
            order.getCustomerEmail());
    }
    
    @Override
    public void notifyOrderStatusChanged(Order order) {
        log.info("📧 [NOTIFICATION] Order status changed - Order ID: {}, Number: {}, Status: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getStatus());
    }
    
    @Override
    public void notifyPaymentFailed(Order order) {
        log.warn("📧 [NOTIFICATION] Payment failed - Order ID: {}, Number: {}, Customer: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerEmail());
    }
    
    @Override
    public void notifyOrderCancelled(Order order) {
        log.info("📧 [NOTIFICATION] Order cancelled - Order ID: {}, Number: {}, Customer: {}", 
            order.getId(), 
            order.getOrderNumber(), 
            order.getCustomerEmail());
    }
}

