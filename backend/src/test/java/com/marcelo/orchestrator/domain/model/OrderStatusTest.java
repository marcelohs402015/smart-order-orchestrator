package com.marcelo.orchestrator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum OrderStatus.
 * 
 * <p>Valida o comportamento da State Machine implementada no enum,
 * garantindo que transições válidas e inválidas funcionam corretamente.</p>
 */
@DisplayName("OrderStatus State Machine Tests")
class OrderStatusTest {
    
    @Test
    @DisplayName("PENDING deve permitir transição para PAID")
    void pendingShouldAllowTransitionToPaid() {
        // Act & Assert
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID));
    }
    
    @Test
    @DisplayName("PENDING deve permitir transição para PAYMENT_FAILED")
    void pendingShouldAllowTransitionToPaymentFailed() {
        // Act & Assert
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAYMENT_FAILED));
    }
    
    @Test
    @DisplayName("PENDING deve permitir transição para CANCELED")
    void pendingShouldAllowTransitionToCanceled() {
        // Act & Assert
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELED));
    }
    
    @Test
    @DisplayName("PAID não deve permitir transições (estado final)")
    void paidShouldNotAllowAnyTransitions() {
        // Act & Assert
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.PAYMENT_FAILED));
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELED));
        assertTrue(OrderStatus.PAID.getAllowedTransitions().isEmpty());
    }
    
    @Test
    @DisplayName("PAYMENT_FAILED deve permitir transição para CANCELED (compensação via saga)")
    void paymentFailedShouldAllowTransitionToCanceled() {
        // Act & Assert
        assertTrue(OrderStatus.PAYMENT_FAILED.canTransitionTo(OrderStatus.CANCELED));
        assertFalse(OrderStatus.PAYMENT_FAILED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.PAYMENT_FAILED.canTransitionTo(OrderStatus.PAID));
        assertEquals(1, OrderStatus.PAYMENT_FAILED.getAllowedTransitions().size());
        assertTrue(OrderStatus.PAYMENT_FAILED.getAllowedTransitions().contains(OrderStatus.CANCELED));
    }
    
    @Test
    @DisplayName("CANCELED não deve permitir transições (estado final)")
    void canceledShouldNotAllowAnyTransitions() {
        // Act & Assert
        assertFalse(OrderStatus.CANCELED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.CANCELED.canTransitionTo(OrderStatus.PAID));
        assertFalse(OrderStatus.CANCELED.canTransitionTo(OrderStatus.PAYMENT_FAILED));
        assertTrue(OrderStatus.CANCELED.getAllowedTransitions().isEmpty());
    }
    
    @Test
    @DisplayName("PENDING não deve permitir transição para si mesmo")
    void pendingShouldNotAllowTransitionToItself() {
        // Act & Assert
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING));
    }
}

