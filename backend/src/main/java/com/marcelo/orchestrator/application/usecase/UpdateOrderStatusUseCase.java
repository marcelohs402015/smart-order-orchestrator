package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para atualização de status de pedido.
 * 
 * <p>Orquestra a mudança de status de um pedido, validando transições
 * permitidas e notificando sobre a mudança.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Buscar pedido pelo ID</li>
 *   <li>Validar transição de status (State Machine no domínio)</li>
 *   <li>Atualizar status do pedido</li>
 *   <li>Persistir mudança</li>
 *   <li>Notificar sobre mudança de status</li>
 * </ul>
 * 
 * <h3>Validação:</h3>
 * <p>A validação de transição é feita pela entidade Order usando o enum OrderStatus.
 * Isso garante que regras de negócio ficam no domínio, não no Use Case.</p>
 * 
 * <h3>Fluxo:</h3>
 * <ol>
 *   <li>Busca pedido</li>
 *   <li>Chama order.updateStatus() (validação no domínio)</li>
 *   <li>Persiste mudança</li>
 *   <li>Notifica sobre mudança</li>
 * </ol>
 * 
 * @author Marcelo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final NotificationPort notificationPort;
    
    /**
     * Atualiza status de um pedido.
     * 
     * @param command Command com ID do pedido e novo status
     * @return Pedido atualizado
     * @throws OrderNotFoundException se pedido não encontrado
     * @throws IllegalStateException se transição não é permitida (validação no domínio)
     */
    @Transactional
    public Order execute(UpdateOrderStatusCommand command) {
        log.info("Updating status for order: {} to {}", command.getOrderId(), command.getNewStatus());
        
        // Buscar pedido
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        // Atualizar status (validação de transição acontece aqui - no domínio)
        order.updateStatus(command.getNewStatus());
        
        // Persistir mudança
        Order updatedOrder = orderRepository.save(order);
        
        // Notificar sobre mudança de status
        try {
            notificationPort.notifyOrderStatusChanged(updatedOrder);
        } catch (Exception e) {
            log.warn("Failed to send notification for order {}: {}", updatedOrder.getId(), e.getMessage());
        }
        
        log.info("Status updated successfully for order: {} - New status: {}",
            updatedOrder.getId(), updatedOrder.getStatus());
        
        return updatedOrder;
    }
}

