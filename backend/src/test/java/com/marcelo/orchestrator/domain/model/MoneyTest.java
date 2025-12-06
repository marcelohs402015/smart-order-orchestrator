package com.marcelo.orchestrator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o Value Object Money.
 * 
 * <p>Valida operações monetárias, imutabilidade e validações do Value Object.</p>
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {
    
    @Test
    @DisplayName("Deve criar Money a partir de BigDecimal")
    void shouldCreateMoneyFromBigDecimal() {
        // Act
        Money money = Money.of(BigDecimal.valueOf(100.50));
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(money.getAmount()));
        assertEquals("BRL", money.getCurrency());
    }
    
    @Test
    @DisplayName("Deve criar Money a partir de double")
    void shouldCreateMoneyFromDouble() {
        // Act
        Money money = Money.of(50.25);
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(50.25).compareTo(money.getAmount()));
    }
    
    @Test
    @DisplayName("Deve arredondar para 2 casas decimais")
    void shouldRoundToTwoDecimalPlaces() {
        // Act
        Money money = Money.of(100.999);
        
        // Assert - Deve arredondar para 101.00
        assertEquals(0, BigDecimal.valueOf(101.00).compareTo(money.getAmount()));
    }
    
    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void shouldThrowExceptionForNegativeAmount() {
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> Money.of(BigDecimal.valueOf(-10))
        );
    }
    
    @Test
    @DisplayName("Deve somar dois valores monetários")
    void shouldAddTwoMoneyValues() {
        // Arrange
        Money money1 = Money.of(100.50);
        Money money2 = Money.of(50.25);
        
        // Act
        Money result = money1.add(money2);
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(150.75).compareTo(result.getAmount()));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao somar moedas diferentes")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        // Arrange
        Money money1 = Money.of(BigDecimal.valueOf(100), "BRL");
        Money money2 = Money.of(BigDecimal.valueOf(50), "USD");
        
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> money1.add(money2)
        );
    }
    
    @Test
    @DisplayName("Deve verificar se valor é zero")
    void shouldCheckIfValueIsZero() {
        // Act & Assert
        assertTrue(Money.ZERO.isZero());
        assertFalse(Money.of(10.50).isZero());
    }
}

