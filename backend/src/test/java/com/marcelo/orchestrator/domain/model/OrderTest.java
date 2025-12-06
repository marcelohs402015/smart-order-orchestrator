package com.marcelo.orchestrator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Order.
 * 
 * <p>Demonstra testes do domínio sem dependências externas (banco, frameworks).
 * Foca em validar regras de negócio e comportamentos da entidade.</p>
 */
@DisplayName("Order Domain Model Tests")
class OrderTest {
    
    @Test
    @DisplayName("Deve calcular total corretamente baseado nos itens")
    void shouldCalculateTotalCorrectly() {
        // Arrange
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
        
        // Act
        order.calculateTotal();
        
        // Assert
        // Total esperado: (2 * 10.50) + (1 * 25.00) = 21.00 + 25.00 = 46.00
        assertEquals(0, BigDecimal.valueOf(46.00).compareTo(order.getTotalAmount()));
    }
    
    @Test
    @DisplayName("Deve permitir transição válida de status")
    void shouldAllowValidStatusTransition() {
        // Arrange
        Order order = createPendingOrder();
        
        // Act & Assert - PENDING pode transicionar para PAID
        assertDoesNotThrow(() -> order.updateStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
    
    @Test
    @DisplayName("Deve lançar exceção para transição inválida de status")
    void shouldThrowExceptionForInvalidStatusTransition() {
        // Arrange
        Order order = createPendingOrder();
        order.updateStatus(OrderStatus.PAID); // Transição válida primeiro
        
        // Act & Assert - PAID não pode voltar para PENDING
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> order.updateStatus(OrderStatus.PENDING)
        );
        
        assertTrue(exception.getMessage().contains("Cannot transition"));
    }
    
    @Test
    @DisplayName("Deve marcar pedido como pago corretamente")
    void shouldMarkOrderAsPaid() {
        // Arrange
        Order order = createPendingOrder();
        String paymentId = "PAY-123456";
        
        // Act
        order.markAsPaid(paymentId);
        
        // Assert
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(paymentId, order.getPaymentId());
        assertTrue(order.isPaid());
    }
    
    @Test
    @DisplayName("Deve marcar pedido como falha de pagamento")
    void shouldMarkOrderAsPaymentFailed() {
        // Arrange
        Order order = createPendingOrder();
        
        // Act
        order.markAsPaymentFailed();
        
        // Assert
        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        assertTrue(order.isPaymentFailed());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao marcar como pago com paymentId nulo")
    void shouldThrowExceptionWhenMarkingAsPaidWithNullPaymentId() {
        // Arrange
        Order order = createPendingOrder();
        
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> order.markAsPaid(null)
        );
    }
    
    // Helper method para criar pedido pendente
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

