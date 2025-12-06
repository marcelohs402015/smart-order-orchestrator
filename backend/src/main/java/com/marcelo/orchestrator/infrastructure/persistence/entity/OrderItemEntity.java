package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA que representa um item de pedido no banco de dados.
 * 
 * <p>Representa a persistência de um {@code OrderItem} do domínio.
 * Relacionamento Many-to-One com OrderEntity.</p>
 * 
 * <h3>Estratégia de Mapeamento:</h3>
 * <ul>
 *   <li><strong>@ManyToOne:</strong> Múltiplos itens pertencem a um pedido</li>
 *   <li><strong>@JoinColumn:</strong> Define coluna de foreign key</li>
 *   <li><strong>FetchType.LAZY:</strong> Carrega itens apenas quando necessário</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {
    
    /**
     * ID único do item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    /**
     * Pedido ao qual este item pertence.
     * Relacionamento Many-to-One.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    /**
     * ID do produto.
     */
    @Column(name = "product_id", nullable = false, columnDefinition = "UUID")
    private UUID productId;
    
    /**
     * Nome do produto (snapshot).
     */
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    /**
     * Quantidade do produto.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * Preço unitário do produto.
     * BigDecimal para precisão monetária.
     */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;
}

