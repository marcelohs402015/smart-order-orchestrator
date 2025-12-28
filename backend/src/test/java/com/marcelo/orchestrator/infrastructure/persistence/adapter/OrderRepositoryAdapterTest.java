package com.marcelo.orchestrator.infrastructure.persistence.adapter;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepositoryAdapter Tests")
class OrderRepositoryAdapterTest {
    
    @Mock
    private JpaOrderRepository jpaOrderRepository;
    
    @Mock
    private OrderPersistenceMapper orderMapper;
    
    @InjectMocks
    private OrderRepositoryAdapter adapter;
    
    private Order testOrder;
    private OrderEntity testEntity;
    
    @BeforeEach
    void setUp() {
        
        testOrder = Order.builder()
            .id(UUID.randomUUID())
            .orderNumber("ORD-123")
            .status(OrderStatus.PENDING)
            .customerId(UUID.randomUUID())
            .customerName("Cliente Teste")
            .customerEmail("cliente@teste.com")
            .items(List.of(
                OrderItem.builder()
                    .productId(UUID.randomUUID())
                    .productName("Produto 1")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.50))
                    .build()
            ))
            .totalAmount(BigDecimal.valueOf(21.00))
            .riskLevel(RiskLevel.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        
        testEntity = new OrderEntity();
        testEntity.setId(testOrder.getId());
        testEntity.setOrderNumber(testOrder.getOrderNumber());
        testEntity.setStatus(testOrder.getStatus());
    }
    
    @Test
    @DisplayName("Deve salvar pedido convertendo domínio para JPA e vice-versa")
    void shouldSaveOrderConvertingDomainToJpaAndBack() {
        
        when(orderMapper.toEntity(testOrder)).thenReturn(testEntity);
        when(jpaOrderRepository.save(testEntity)).thenReturn(testEntity);
        when(orderMapper.toDomain(testEntity)).thenReturn(testOrder);
        
        
        Order savedOrder = adapter.save(testOrder);
        
        
        assertNotNull(savedOrder);
        verify(orderMapper).toEntity(testOrder);
        verify(jpaOrderRepository).save(testEntity);
        verify(orderMapper).toDomain(testEntity);
    }
    
    @Test
    @DisplayName("Deve buscar pedido por ID")
    void shouldFindOrderById() {
        
        UUID orderId = testOrder.getId();
        when(jpaOrderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testEntity));
        when(orderMapper.toDomain(testEntity)).thenReturn(testOrder);
        
        
        Optional<Order> result = adapter.findById(orderId);
        
        
        assertTrue(result.isPresent());
        assertEquals(testOrder.getId(), result.get().getId());
        verify(jpaOrderRepository).findByIdWithItems(orderId);
        verify(orderMapper).toDomain(testEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando pedido não encontrado")
    void shouldReturnEmptyWhenOrderNotFound() {
        
        UUID orderId = UUID.randomUUID();
        when(jpaOrderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());
        
        
        Optional<Order> result = adapter.findById(orderId);
        
        
        assertTrue(result.isEmpty());
        verify(jpaOrderRepository).findByIdWithItems(orderId);
        verify(orderMapper, never()).toDomain(any());
    }
}

