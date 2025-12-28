package com.marcelo.orchestrator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {
    
    @Test
    @DisplayName("Deve criar Money a partir de BigDecimal")
    void shouldCreateMoneyFromBigDecimal() {
        Money money = Money.of(BigDecimal.valueOf(100.50));
        
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(money.getAmount()));
        assertEquals("BRL", money.getCurrency());
    }
    
    @Test
    @DisplayName("Deve criar Money a partir de double")
    void shouldCreateMoneyFromDouble() {
        Money money = Money.of(50.25);
        
        assertEquals(0, BigDecimal.valueOf(50.25).compareTo(money.getAmount()));
    }
    
    @Test
    @DisplayName("Deve arredondar para 2 casas decimais")
    void shouldRoundToTwoDecimalPlaces() {
        Money money = Money.of(100.999);
        
        assertEquals(0, BigDecimal.valueOf(101.00).compareTo(money.getAmount()));
    }
    
    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void shouldThrowExceptionForNegativeAmount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Money.of(BigDecimal.valueOf(-10))
        );
    }
    
    @Test
    @DisplayName("Deve somar dois valores monetários")
    void shouldAddTwoMoneyValues() {
        Money money1 = Money.of(100.50);
        Money money2 = Money.of(50.25);
        
        Money result = money1.add(money2);
        
        assertEquals(0, BigDecimal.valueOf(150.75).compareTo(result.getAmount()));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao somar moedas diferentes")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money money1 = Money.of(BigDecimal.valueOf(100), "BRL");
        Money money2 = Money.of(BigDecimal.valueOf(50), "USD");
        
        assertThrows(
            IllegalArgumentException.class,
            () -> money1.add(money2)
        );
    }
    
    @Test
    @DisplayName("Deve verificar se valor é zero")
    void shouldCheckIfValueIsZero() {
        assertTrue(Money.ZERO.isZero());
        assertFalse(Money.of(10.50).isZero());
    }
}

