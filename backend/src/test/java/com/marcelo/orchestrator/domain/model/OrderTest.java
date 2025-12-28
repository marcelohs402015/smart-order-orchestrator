package com.marcelo.orchestrator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Domain Model Tests")
class OrderTest {
    
    @Test
    @DisplayName("Deve calcular total corretamente baseado nos itens")
    void shouldCalculateTotalCorrectly() {
        
        OrderItem item1 = OrderItem.builder()
            .productId(UUID.randomUUID())
            .productName("Produto 1")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10.50))
            .build();
        
        OrderItem item2 = OrderItem.builder()
            .productId(UUID.randomUUID())
            .productName("Produto 2")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(25.00))
            .build();
        
        Order order = Order.builder()
            .id(UUID.randomUUID())
            .orderNumber("ORD-123")
            .status(OrderStatus.PENDING)
            .customerId(UUID.randomUUID())
            .customerName("Cliente Teste")
            .customerEmail("cliente@teste.com")
            .items(List.of(item1, item2))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        
        order.calculateTotal();
        
        
        
        assertEquals(0, BigDecimal.valueOf(46.00).compareTo(order.getTotalAmount()));
    }
    
    @Test
    @DisplayName("Deve permitir transição válida de status")
    void shouldAllowValidStatusTransition() {
        
        Order order = createPendingOrder();
        
        
        assertDoesNotThrow(() -> order.updateStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
    
    @Test
    @DisplayName("Deve lançar exceção para transição inválida de status")
    void shouldThrowExceptionForInvalidStatusTransition() {
        
        Order order = createPendingOrder();
        order.updateStatus(OrderStatus.PAID); 
        
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> order.updateStatus(OrderStatus.PENDING)
        );
        
        assertTrue(exception.getMessage().contains("Cannot transition"));
    }
    
    @Test
    @DisplayName("Deve marcar pedido como pago corretamente")
    void shouldMarkOrderAsPaid() {
        
        Order order = createPendingOrder();
        String paymentId = "PAY-123456";
        
        
        order.markAsPaid(paymentId);
        
        
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(paymentId, order.getPaymentId());
        assertTrue(order.isPaid());
    }
    
    @Test
    @DisplayName("Deve marcar pedido como falha de pagamento")
    void shouldMarkOrderAsPaymentFailed() {
        
        Order order = createPendingOrder();
        
        
        order.markAsPaymentFailed();
        
        
        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        assertTrue(order.isPaymentFailed());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao marcar como pago com paymentId nulo")
    void shouldThrowExceptionWhenMarkingAsPaidWithNullPaymentId() {
        
        Order order = createPendingOrder();
        
        
        assertThrows(
            IllegalArgumentException.class,
            () -> order.markAsPaid(null)
        );
    }
    
    
    private Order createPendingOrder() {
        return Order.builder()
            .id(UUID.randomUUID())
            .orderNumber("ORD-123")
            .status(OrderStatus.PENDING)
            .customerId(UUID.randomUUID())
            .customerName("Cliente Teste")
            .customerEmail("cliente@teste.com")
            .items(List.of())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}

