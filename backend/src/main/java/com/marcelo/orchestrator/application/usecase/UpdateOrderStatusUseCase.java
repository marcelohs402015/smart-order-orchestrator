package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

