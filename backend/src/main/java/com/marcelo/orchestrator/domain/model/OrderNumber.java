package com.marcelo.orchestrator.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa o número único de um pedido.
 * 
 * <p>Implementa o padrão <strong>Value Object</strong> do DDD.
 * Encapsula a lógica de geração e validação de números de pedido.</p>
 * 
 * <h3>Por que Value Object?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Número do pedido não muda após criação</li>
 *   <li><strong>Validação:</strong> Garante formato válido (ex: "ORD-1234567890")</li>
 *   <li><strong>Type Safety:</strong> Não confundir com String genérica</li>
 *   <li><strong>Encapsulamento:</strong> Lógica de geração e validação centralizada</li>
 * </ul>
 * 
 * <h3>Formato:</h3>
 * <p>Padrão: "ORD-" seguido de timestamp ou número sequencial.
 * Exemplo: "ORD-1703123456789" ou "ORD-20240101001"</p>
 * 
 * @author Marcelo
 */
public final class OrderNumber {
    
    /**
     * Prefixo padrão para números de pedido.
     */
    private static final String PREFIX = "ORD-";
    
    /**
     * Pattern para validação do formato.
     * Aceita: ORD- seguido de números
     */
    private static final Pattern VALID_PATTERN = Pattern.compile("^ORD-\\d+$");
    
    /**
     * Valor do número do pedido (ex: "ORD-1703123456789").
     */
    private final String value;
    
    /**
     * Construtor privado para garantir criação através de factory methods.
     * 
     * @param value Valor do número do pedido
     * @throws IllegalArgumentException se formato for inválido
     */
    private OrderNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order number cannot be null or blank");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException(
                String.format("Invalid order number format: %s. Expected format: ORD-<numbers>", value)
            );
        }
        this.value = value;
    }
    
    /**
     * Factory method para criar OrderNumber a partir de String.
     * 
     * @param value String com número do pedido
     * @return Instância de OrderNumber
     * @throws IllegalArgumentException se formato for inválido
     */
    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }
    
    /**
     * Gera um novo número de pedido baseado em timestamp.
     * 
     * <p>Usa timestamp atual para garantir unicidade.
     * Formato: ORD-<timestamp>
     * Exemplo: ORD-1703123456789</p>
     * 
     * @return Novo OrderNumber gerado
     */
    public static OrderNumber generate() {
        long timestamp = System.currentTimeMillis();
        return new OrderNumber(PREFIX + timestamp);
    }
    
    /**
     * Gera um número de pedido com sufixo customizado.
     * 
     * @param suffix Sufixo numérico (ex: sequencial)
     * @return Novo OrderNumber gerado
     */
    public static OrderNumber generate(long suffix) {
        return new OrderNumber(PREFIX + suffix);
    }
    
    /**
     * Valida o formato do número do pedido.
     * 
     * @param value String a validar
     * @return {@code true} se formato é válido
     */
    private static boolean isValidFormat(String value) {
        return VALID_PATTERN.matcher(value).matches();
    }
    
    /**
     * Retorna o valor do número do pedido.
     * 
     * @return String com número do pedido
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Extrai o sufixo numérico do número do pedido.
     * 
     * @return Long com sufixo numérico
     */
    public long getNumericSuffix() {
        String suffix = value.substring(PREFIX.length());
        return Long.parseLong(suffix);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

