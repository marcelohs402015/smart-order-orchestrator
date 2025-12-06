package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value Object que representa um item de pedido.
 * 
 * <p>Implementa o padrão <strong>Value Object</strong> do Domain-Driven Design (DDD).
 * Value Objects são imutáveis e definidos apenas por seus atributos, não por identidade.</p>
 * 
 * <h3>Por que Value Object?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Uma vez criado, não pode ser alterado.
 *       Garante thread-safety e previne bugs de estado compartilhado.</li>
 *   <li><strong>Encapsulamento:</strong> Lógica de cálculo (subtotal) fica no próprio objeto,
 *       não espalhada em services.</li>
 *   <li><strong>Sem JPA:</strong> Este é um objeto de domínio puro, sem anotações de persistência.
 *       A camada Infrastructure criará uma entidade JPA separada para persistência.</li>
 * </ul>
 * 
 * <h3>Separação de Concerns:</h3>
 * <p>Este objeto representa o <strong>conceito de negócio</strong> de um item de pedido.
 * A persistência será tratada por uma entidade JPA na camada Infrastructure,
 * mantendo o domínio independente de frameworks e tecnologias.</p>
 * 
 * <h3>Benefícios desta Abordagem:</h3>
 * <ul>
 *   <li>Domínio testável sem necessidade de banco de dados</li>
 *   <li>Fácil trocar implementação de persistência (JPA, MongoDB, etc.)</li>
 *   <li>Regras de negócio isoladas e reutilizáveis</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class OrderItem {
    
    /**
     * Identificador único do produto.
     */
    private final UUID productId;
    
    /**
     * Nome do produto (snapshot no momento do pedido).
     * Armazenado aqui para manter histórico mesmo se o produto for alterado depois.
     */
    private final String productName;
    
    /**
     * Quantidade do produto no pedido.
     * Deve ser maior que zero (validação pode ser adicionada no construtor).
     */
    private final Integer quantity;
    
    /**
     * Preço unitário do produto no momento do pedido.
     * Usa BigDecimal para precisão em cálculos monetários (evita problemas de ponto flutuante).
     */
    private final BigDecimal unitPrice;
    
    /**
     * Calcula o subtotal deste item (quantidade × preço unitário).
     * 
     * <p>Lógica de negócio encapsulada no próprio Value Object.
     * Isso segue o princípio de "Tell, Don't Ask" - o objeto sabe calcular seu próprio subtotal.</p>
     * 
     * @return Subtotal calculado (quantity × unitPrice)
     */
    public BigDecimal getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

