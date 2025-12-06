package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA que representa um pedido no banco de dados.
 * 
 * <p>Esta é a <strong>entidade de persistência</strong>, separada da entidade de domínio ({@code Order}).
 * Esta separação garante que o domínio não depende de JPA/Hibernate.</p>
 * 
 * <h3>Por que Entidade JPA Separada?</h3>
 * <ul>
 *   <li><strong>Separação de Concerns:</strong> Persistência vs. Regras de Negócio</li>
 *   <li><strong>Independência do Domínio:</strong> Domínio não conhece JPA</li>
 *   <li><strong>Flexibilidade:</strong> Pode ter campos diferentes (ex: campos técnicos de auditoria)</li>
 *   <li><strong>Otimização:</strong> Pode otimizar para banco (índices, constraints) sem afetar domínio</li>
 * </ul>
 * 
 * <h3>Mapeamento:</h3>
 * <p>Esta entidade será mapeada para/do domínio usando MapStruct.
 * O adapter na Infrastructure faz a conversão entre OrderEntity e Order.</p>
 * 
 * <h3>Estratégia de Mapeamento:</h3>
 * <ul>
 *   <li><strong>@OneToMany:</strong> Order tem múltiplos OrderItemEntity</li>
 *   <li><strong>CascadeType.ALL:</strong> Salvar/carregar itens junto com pedido</li>
 *   <li><strong>@Enumerated:</strong> Enums salvos como String (mais legível e flexível)</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    
    /**
     * ID único do pedido (UUID).
     * Usa UUID ao invés de Long para evitar problemas de sequência em sistemas distribuídos.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    /**
     * Número único do pedido (ex: "ORD-1234567890").
     * Único e indexado para buscas rápidas.
     */
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;
    
    /**
     * Status do pedido.
     * Enum salvo como String para legibilidade e flexibilidade.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private com.marcelo.orchestrator.domain.model.OrderStatus status;
    
    /**
     * ID do cliente.
     */
    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;
    
    /**
     * Nome do cliente (snapshot).
     */
    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;
    
    /**
     * Email do cliente (snapshot).
     */
    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;
    
    /**
     * Itens do pedido.
     * Relacionamento One-to-Many com cascade para persistir junto.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> items;
    
    /**
     * Valor total do pedido.
     * BigDecimal para precisão monetária.
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    /**
     * ID do pagamento no gateway externo.
     */
    @Column(name = "payment_id", length = 100)
    private String paymentId;
    
    /**
     * Nível de risco após análise por IA.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private com.marcelo.orchestrator.domain.model.RiskLevel riskLevel;
    
    /**
     * Data e hora de criação.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Data e hora da última atualização.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Versão para controle de concorrência otimista (JPA Optimistic Locking).
     * Previne perda de atualizações em ambientes concorrentes.
     */
    @Version
    @Column(name = "version")
    private Long version;
}

