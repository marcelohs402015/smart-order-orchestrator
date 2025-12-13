package com.marcelo.orchestrator.infrastructure.persistence.adapter;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import com.marcelo.orchestrator.infrastructure.persistence.mapper.OrderMapper;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa OrderRepositoryPort usando JPA.
 * 
 * <p>Este é o <strong>Adapter</strong> na Arquitetura Hexagonal.
 * Converte entre entidades de domínio ({@code Order}) e entidades JPA ({@code OrderEntity}),
 * implementando a porta definida no domínio.</p>
 * 
 * <h3>Padrão Adapter (Hexagonal Architecture):</h3>
 * <ul>
 *   <li><strong>Port:</strong> OrderRepositoryPort (definida no domínio)</li>
 *   <li><strong>Adapter:</strong> Esta classe (implementa a porta)</li>
 *   <li><strong>Inversão de Dependência:</strong> Domínio não conhece esta implementação</li>
 * </ul>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Converter Order (domínio) ↔ OrderEntity (JPA)</li>
 *   <li>Chamar JpaOrderRepository (Spring Data JPA)</li>
 *   <li>Tratar erros de persistência</li>
 * </ul>
 * 
 * <h3>Mapeamento:</h3>
 * <p>Usa MapStruct para conversão automática entre domínio e JPA.
 * Isso reduz boilerplate e garante consistência.</p>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    
    private final JpaOrderRepository jpaOrderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public Order save(Order order) {
        log.debug("Saving order: {}", order.getId());
        
        // Verificar se entidade já existe no banco (para evitar "detached entity" error)
        // IMPORTANTE: Usar findByIdWithItems() para carregar itens junto (evita lazy loading issues)
        OrderEntity entity = jpaOrderRepository.findByIdWithItems(order.getId())
            .orElse(null);
        
        if (entity != null) {
            // Entidade existe: atualizar campos existentes
            // Isso evita erro "A different object with the same identifier value was already associated with the session"
            log.debug("Order {} already exists, updating existing entity", order.getId());
            
            // IMPORTANTE: Atualizar apenas campos simples (status, paymentId, etc.)
            // NÃO atualizar itens - eles só mudam na criação do pedido
            // Atualizar itens quebra a referência gerenciada pelo JPA com orphanRemoval=true
            orderMapper.updateEntity(order, entity);
            
            // NÃO atualizar itens aqui - eles são imutáveis após criação
            // Os itens só são definidos na criação do pedido e não devem ser alterados
            // Se precisar alterar itens, deve ser feito através de uma nova entidade ou
            // através de uma operação específica que lide com isso corretamente
        } else {
            // Entidade não existe: criar nova
            log.debug("Order {} does not exist, creating new entity", order.getId());
            entity = orderMapper.toEntity(order);
        }
        
        // Salvar usando Spring Data JPA (save() faz merge se entidade já existe)
        OrderEntity savedEntity = jpaOrderRepository.save(entity);
        
        // Converter JPA para domínio
        return orderMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(UUID id) {
        log.debug("Finding order by ID: {}", id);
        
        // Usa query com JOIN FETCH para carregar items junto (evita LazyInitializationException)
        return jpaOrderRepository.findByIdWithItems(id)
            .map(orderMapper::toDomain);
    }
    
    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        log.debug("Finding order by number: {}", orderNumber);
        
        return jpaOrderRepository.findByOrderNumber(orderNumber)
            .map(orderMapper::toDomain);
    }
    
    @Override
    public List<Order> findAll() {
        log.debug("Finding all orders");
        
        // Usa query com JOIN FETCH para carregar items junto (evita LazyInitializationException)
        return jpaOrderRepository.findAllWithItems().stream()
            .map(orderMapper::toDomain)
            .toList(); // Java 16+ - mais conciso que Collectors.toList()
    }
    
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        log.debug("Finding orders by status: {}", status);
        
        return jpaOrderRepository.findByStatus(status).stream()
            .map(orderMapper::toDomain)
            .toList(); // Java 16+ - mais conciso que Collectors.toList()
    }
    
    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting order: {}", id);
        jpaOrderRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaOrderRepository.existsById(id);
    }
}

