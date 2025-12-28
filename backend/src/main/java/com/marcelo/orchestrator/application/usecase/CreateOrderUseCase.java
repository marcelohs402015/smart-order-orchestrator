package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.*;
import com.marcelo.orchestrator.domain.port.NotificationPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final NotificationPort notificationPort;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order execute(CreateOrderCommand command) {
        log.info("Creating order for customer: {}", command.getCustomerId());
        
        // Validação de entrada
        validateCommand(command);
        
        // Criar entidade de domínio
        Order order = Order.builder()
            .id(UUID.randomUUID())
            .orderNumber(OrderNumber.generate().getValue())
            .status(OrderStatus.PENDING)
            .customerId(command.getCustomerId())
            .customerName(command.getCustomerName())
            .customerEmail(command.getCustomerEmail())
            .items(command.getItems())
            .riskLevel(RiskLevel.PENDING) // Inicialmente pendente, será analisado depois
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Calcular total (regra de negócio no domínio)
        order.calculateTotal();
        
        // Persistir através da porta (infraestrutura implementa)
        Order savedOrder = orderRepository.save(order);
        
        // Notificar (infraestrutura implementa - pode ser email, webhook, etc.)
        try {
            notificationPort.notifyOrderCreated(savedOrder);
        } catch (Exception e) {
            // Log mas não falha a transação (notificação não é crítica)
            log.warn("Failed to send notification for order {}: {}", savedOrder.getId(), e.getMessage());
        }
        
        log.info("Order created successfully: {} with number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        return savedOrder;
    }
    
    private void validateCommand(CreateOrderCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (command.getCustomerEmail() == null || command.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be null or blank");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }
}

