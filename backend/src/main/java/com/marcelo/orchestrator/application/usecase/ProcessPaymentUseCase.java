package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para processamento de pagamento.
 * 
 * <p>Orquestra o fluxo de pagamento de um pedido, integrando com gateway externo
 * e atualizando o status do pedido conforme o resultado.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Buscar pedido pelo ID</li>
 *   <li>Validar que pedido está em estado válido para pagamento</li>
 *   <li>Chamar gateway de pagamento (com Circuit Breaker via Resilience4j)</li>
 *   <li>Atualizar status do pedido baseado no resultado</li>
 *   <li>Notificar sobre resultado do pagamento</li>
 * </ul>
 * 
 * <h3>Resiliência:</h3>
 * <p>A chamada ao PaymentGatewayPort será protegida por Circuit Breaker
 * configurado na camada Infrastructure. Se gateway estiver indisponível,
 * o Circuit Breaker abre e retorna falha rapidamente.</p>
 * 
 * <h3>Fluxo:</h3>
 * <ol>
 *   <li>Valida que pedido existe e está PENDING</li>
 *   <li>Cria PaymentRequest com dados do pedido</li>
 *   <li>Chama PaymentGatewayPort (pode falhar - Circuit Breaker protege)</li>
 *   <li>Se sucesso: marca pedido como PAID</li>
 *   <li>Se falha: marca pedido como PAYMENT_FAILED</li>
 *   <li>Persiste mudança e notifica</li>
 * </ol>
 * 
 * @author Marcelo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final PaymentGatewayPort paymentGateway;
    private final NotificationPort notificationPort;
    
    /**
     * Processa pagamento de um pedido.
     * 
     * @param command Command com dados do pagamento
     * @return Pedido atualizado com resultado do pagamento
     * @throws IllegalArgumentException se pedido não encontrado ou estado inválido
     */
    @Transactional
    public Order execute(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.getOrderId());
        
        // Buscar pedido
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Order not found: %s", command.getOrderId())
            ));
        
        // Validar estado
        if (!order.isPending()) {
            throw new IllegalStateException(
                String.format("Order %s is not in PENDING status. Current status: %s",
                    order.getId(), order.getStatus())
            );
        }
        
        // Criar requisição de pagamento
        PaymentRequest paymentRequest = new PaymentRequest(
            order.getId(),
            order.getTotalAmount(),
            command.getCurrency(),
            command.getPaymentMethod(),
            order.getCustomerEmail()
        );
        
        // Processar pagamento no gateway (Circuit Breaker protege esta chamada)
        PaymentResult paymentResult = paymentGateway.processPayment(paymentRequest);
        
        // Atualizar pedido baseado no resultado
        if (paymentResult.isSuccessful()) {
            order.markAsPaid(paymentResult.paymentId());
            log.info("Payment successful for order: {} - Payment ID: {}",
                order.getId(), paymentResult.paymentId());
        } else {
            order.markAsPaymentFailed();
            log.warn("Payment failed for order: {} - Reason: {}",
                order.getId(), paymentResult.message());
        }
        
        // Persistir mudança
        Order updatedOrder = orderRepository.save(order);
        
        // Notificar
        try {
            if (paymentResult.isSuccessful()) {
                notificationPort.notifyOrderStatusChanged(updatedOrder);
            } else {
                notificationPort.notifyPaymentFailed(updatedOrder);
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for order {}: {}", updatedOrder.getId(), e.getMessage());
        }
        
        return updatedOrder;
    }
}

