package com.marcelo.orchestrator.infrastructure.persistence.adapter;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import com.marcelo.orchestrator.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    
    private final JpaOrderRepository jpaOrderRepository;
    private final OrderPersistenceMapper orderMapper;
    
    @Override
    public Order save(Order order) {
        log.debug("Saving order: {}", order.getId());
        
        
        
        OrderEntity entity = jpaOrderRepository.findByIdWithItems(order.getId())
            .orElse(null);
        
        if (entity != null) {
            
            
            log.debug("Order {} already exists, updating existing entity", order.getId());
            
            
            
            
            orderMapper.updateEntity(order, entity);
            
            
            
            
            
        } else {
            
            log.debug("Order {} does not exist, creating new entity", order.getId());
            entity = orderMapper.toEntity(order);
        }
        
        
        OrderEntity savedEntity = jpaOrderRepository.save(entity);
        
        
        return orderMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Order> findById(UUID id) {
        log.debug("Finding order by ID: {}", id);
        
        
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
    public Optional<Order> findByPaymentId(String paymentId) {
        log.debug("Finding order by paymentId: {}", paymentId);
        
        return jpaOrderRepository.findByPaymentId(paymentId)
            .map(orderMapper::toDomain);
    }
    
    @Override
    public List<Order> findAll() {
        log.debug("Finding all orders");
        
        
        return jpaOrderRepository.findAllWithItems().stream()
            .map(orderMapper::toDomain)
            .toList(); 
    }
    
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        log.debug("Finding orders by status: {}", status);
        
        return jpaOrderRepository.findByStatus(status).stream()
            .map(orderMapper::toDomain)
            .toList(); 
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

