package com.marcelo.orchestrator.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa uma quantidade monetária.
 * 
 * <p>Implementa o padrão <strong>Value Object</strong> do DDD.
 * Encapsula valores monetários com precisão e operações seguras.</p>
 * 
 * <h3>Por que Value Object para Dinheiro?</h3>
 * <ul>
 *   <li><strong>Precisão:</strong> Usa BigDecimal para evitar problemas de ponto flutuante</li>
 *   <li><strong>Imutabilidade:</strong> Valores monetários não mudam após criação</li>
 *   <li><strong>Encapsulamento:</strong> Operações matemáticas seguras (soma, subtração, etc.)</li>
 *   <li><strong>Validação:</strong> Garante que valores não são negativos (regra de negócio)</li>
 * </ul>
 * 
 * <h3>Por que não usar BigDecimal diretamente?</h3>
 * <p>Money encapsula BigDecimal e adiciona:
 * - Validações de negócio (não negativo, precisão)
 * - Operações semânticas (add, subtract, multiply)
 * - Formatação consistente
 * - Type safety (não confundir com outros BigDecimal)</p>
 * 
 * @author Marcelo
 */
public final class Money {
    
    /**
     * Valor zero (singleton para reutilização).
     */
    public static final Money ZERO = new Money(BigDecimal.ZERO, "BRL");
    
    /**
     * Valor monetário (BigDecimal para precisão).
     * Sempre com 2 casas decimais (centavos).
     */
    private final BigDecimal amount;
    
    /**
     * Moeda (padrão: BRL - Real Brasileiro).
     */
    private final String currency;
    
    /**
     * Construtor privado para garantir criação através de factory methods.
     * 
     * @param amount Valor monetário
     * @param currency Moeda (ex: "BRL", "USD")
     */
    private Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or blank");
        }
        
        // Arredonda para 2 casas decimais (centavos)
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }
    
    /**
     * Factory method para criar Money em BRL (Real Brasileiro).
     * 
     * @param amount Valor em BigDecimal
     * @return Instância de Money
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, "BRL");
    }
    
    /**
     * Factory method para criar Money a partir de double.
     * 
     * @param amount Valor em double
     * @return Instância de Money
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }
    
    /**
     * Factory method para criar Money com moeda específica.
     * 
     * @param amount Valor em BigDecimal
     * @param currency Moeda (ex: "BRL", "USD")
     * @return Instância de Money
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
    
    /**
     * Retorna o valor como BigDecimal.
     * 
     * @return BigDecimal com o valor (imutável)
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Retorna a moeda.
     * 
     * @return String com código da moeda
     */
    public String getCurrency() {
        return currency;
    }
    
    /**
     * Soma dois valores monetários.
     * 
     * @param other Outro valor monetário (deve ter mesma moeda)
     * @return Novo Money com resultado da soma
     * @throws IllegalArgumentException se moedas forem diferentes
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtrai dois valores monetários.
     * 
     * @param other Valor a subtrair
     * @return Novo Money com resultado da subtração
     * @throws IllegalArgumentException se moedas forem diferentes ou resultado negativo
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplica o valor por um número.
     * 
     * @param multiplier Multiplicador
     * @return Novo Money com resultado da multiplicação
     */
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
    
    /**
     * Verifica se o valor é zero.
     * 
     * @return {@code true} se valor é zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Verifica se este valor é maior que outro.
     * 
     * @param other Outro valor monetário
     * @return {@code true} se este valor é maior
     */
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", currency, amount);
    }
}

