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

/**
 * Use Case para criação de pedidos.
 * 
 * <p>Implementa o padrão <strong>Use Case</strong> da Clean Architecture.
 * Um Use Case representa uma ação que o sistema pode executar, encapsulando
 * a lógica de orquestração necessária para completar essa ação.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Orquestrar criação de pedido</li>
 *   <li>Validar dados de entrada</li>
 *   <li>Calcular total do pedido</li>
 *   <li>Persistir pedido</li>
 *   <li>Notificar sobre criação</li>
 * </ul>
 * 
 * <h3>Por que Use Case separado?</h3>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Uma classe, uma responsabilidade</li>
 *   <li><strong>Testabilidade:</strong> Fácil testar isoladamente (mock das portas)</li>
 *   <li><strong>Reutilização:</strong> Pode ser chamado por diferentes adaptadores (REST, CLI, etc.)</li>
 *   <li><strong>Orquestração:</strong> Coordena múltiplas operações sem acoplar ao domínio</li>
 * </ul>
 * 
 * <h3>Fluxo:</h3>
 * <ol>
 *   <li>Recebe command com dados do pedido</li>
 *   <li>Cria entidade Order com status PENDING</li>
 *   <li>Calcula total baseado nos itens</li>
 *   <li>Persiste através do OrderRepositoryPort</li>
 *   <li>Notifica através do NotificationPort</li>
 *   <li>Retorna pedido criado</li>
 * </ol>
 * 
 * @author Marcelo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final NotificationPort notificationPort;
    
    /**
     * Executa o caso de uso de criação de pedido.
     * 
     * <p>Método transacional que garante consistência: se notificação falhar,
     * a transação pode ser revertida (dependendo da configuração).</p>
     * 
     * <p><strong>Padrão Saga:</strong> Usa REQUIRES_NEW para garantir transação
     * independente que faz commit imediato, permitindo compensação manual se
     * passos subsequentes falharem.</p>
     * 
     * @param command Dados do pedido a ser criado
     * @return Pedido criado e persistido
     * @throws IllegalArgumentException se dados inválidos
     */
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
    
    /**
     * Valida o command antes de processar.
     * 
     * <p>Validações de aplicação (não regras de negócio do domínio).
     * Regras de negócio ficam na entidade Order.</p>
     * 
     * @param command Command a validar
     * @throws IllegalArgumentException se dados inválidos
     */
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

