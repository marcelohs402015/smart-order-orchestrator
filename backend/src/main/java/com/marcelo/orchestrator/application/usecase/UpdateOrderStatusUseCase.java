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
    
    
    @Transactional
    public Order execute(UpdateOrderStatusCommand command) {
        log.info("Updating status for order: {} to {}", command.getOrderId(), command.getNewStatus());
        
        
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        
        order.updateStatus(command.getNewStatus());
        
        
        Order updatedOrder = orderRepository.save(order);
        
        
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

