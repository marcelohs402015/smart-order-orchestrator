package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidade de domínio rica que representa um pedido.
 * 
 * <p>Implementa o padrão <strong>Rich Domain Model</strong> do Domain-Driven Design (DDD),
 * onde as regras de negócio estão encapsuladas na própria entidade, não em services externos.</p>
 * 
 * <h3>Por que Rich Domain Model?</h3>
 * <ul>
 *   <li><strong>Encapsulamento:</strong> Regras de negócio ficam próximas aos dados que manipulam,
 *       facilitando manutenção e compreensão.</li>
 *   <li><strong>Coesão:</strong> Tudo relacionado a um pedido está em um único lugar.</li>
 *   <li><strong>Testabilidade:</strong> Regras de negócio podem ser testadas sem dependências externas.</li>
 *   <li><strong>Sem Anemia:</strong> Evita "Anemic Domain Model" onde entidades são apenas DTOs
 *       e toda lógica fica em services.</li>
 * </ul>
 * 
 * <h3>Por que SEM anotações JPA aqui?</h3>
 * <p>Este é um objeto de <strong>domínio puro</strong>, sem dependências de infraestrutura.
 * A camada Infrastructure criará uma entidade JPA separada ({@code OrderEntity}) que será
 * mapeada para este objeto de domínio. Isso garante:</p>
 * <ul>
 *   <li><strong>Independência de frameworks:</strong> Domínio não depende de JPA/Hibernate</li>
 *   <li><strong>Testabilidade:</strong> Testes de domínio não precisam de banco de dados</li>
 *   <li><strong>Flexibilidade:</strong> Fácil trocar JPA por MongoDB, Cassandra, etc.</li>
 *   <li><strong>Separação de Concerns:</strong> Regras de negócio vs. persistência</li>
 * </ul>
 * 
 * <h3>Métodos de Negócio:</h3>
 * <ul>
 *   <li>{@link #calculateTotal()}: Calcula total baseado nos itens</li>
 *   <li>{@link #updateStatus(OrderStatus)}: Atualiza status com validação de transição</li>
 *   <li>{@link #markAsPaid(String)}: Marca pedido como pago</li>
 *   <li>{@link #markAsPaymentFailed()}: Marca falha de pagamento</li>
 * </ul>
 * 
 * <h3>Imutabilidade Parcial:</h3>
 * <p>Alguns campos são mutáveis (status, paymentId, riskLevel, updatedAt) porque representam
 * mudanças de estado do pedido ao longo do tempo. Campos imutáveis (id, orderNumber, createdAt)
 * garantem identidade e rastreabilidade.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class Order {
    
    /**
     * Identificador único do pedido (UUID).
     * Imutável após criação - garante identidade única.
     */
    private final UUID id;
    
    /**
     * Número único do pedido (ex: "ORD-1234567890").
     * Usado para identificação externa e rastreabilidade.
     * Imutável após criação.
     */
    private final String orderNumber;
    
    /**
     * Status atual do pedido.
     * Mutável - pode transicionar entre estados válidos.
     */
    private OrderStatus status;
    
    /**
     * Identificador do cliente.
     */
    private final UUID customerId;
    
    /**
     * Nome do cliente (snapshot no momento do pedido).
     * Armazenado aqui para manter histórico mesmo se dados do cliente mudarem.
     */
    private final String customerName;
    
    /**
     * Email do cliente (snapshot no momento do pedido).
     */
    private final String customerEmail;
    
    /**
     * Lista de itens do pedido.
     * Imutável após criação - itens não podem ser adicionados/removidos depois.
     */
    private final List<OrderItem> items;
    
    /**
     * Valor total do pedido.
     * Calculado através do método {@link #calculateTotal()}.
     * Mutável - pode ser recalculado se necessário.
     */
    private BigDecimal totalAmount;
    
    /**
     * ID do pagamento no gateway externo (opcional).
     * Preenchido quando pagamento é processado com sucesso.
     */
    private String paymentId;
    
    /**
     * Nível de risco após análise por IA (opcional).
     * Preenchido após análise de risco ser concluída.
     */
    private RiskLevel riskLevel;
    
    /**
     * Data e hora de criação do pedido.
     * Imutável - timestamp de criação.
     */
    private final LocalDateTime createdAt;
    
    /**
     * Data e hora da última atualização.
     * Atualizado automaticamente em mudanças de estado.
     */
    private LocalDateTime updatedAt;

    /**
     * Calcula o valor total do pedido baseado nos itens.
     * 
     * <p>Encapsula a lógica de cálculo no próprio domínio.
     * Soma os subtotais de todos os itens do pedido.</p>
     * 
     * <p><strong>Regra de Negócio:</strong> Total = Σ (item.quantity × item.unitPrice)</p>
     */
    public void calculateTotal() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Atualiza o status do pedido com validação de transição.
     * 
     * <p>Implementa validação de transição de estado usando o padrão State Machine.
     * Previne transições inválidas que poderiam causar inconsistências no domínio.</p>
     * 
     * @param newStatus Novo status desejado
     * @throws IllegalStateException se a transição não é permitida
     */
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        if (this.status == null) {
            // Primeira atribuição de status
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now();
            return;
        }
        
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                    this.status, newStatus, this.status.getAllowedTransitions())
            );
        }
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca o pedido como pago após processamento bem-sucedido do pagamento.
     * 
     * <p>Atualiza status para PAID e armazena o ID do pagamento para rastreabilidade.
     * Valida que a transição é permitida antes de executar.</p>
     * 
     * @param paymentId ID do pagamento no gateway externo
     * @throws IllegalStateException se não é possível transicionar para PAID
     */
    public void markAsPaid(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be null or blank");
        }
        
        updateStatus(OrderStatus.PAID);
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca o pedido como falha de pagamento.
     * 
     * <p>Atualiza status para PAYMENT_FAILED quando o processamento do pagamento falha.
     * Valida que a transição é permitida antes de executar.</p>
     * 
     * @throws IllegalStateException se não é possível transicionar para PAYMENT_FAILED
     */
    public void markAsPaymentFailed() {
        updateStatus(OrderStatus.PAYMENT_FAILED);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o pedido está pendente (aguardando processamento).
     * 
     * @return {@code true} se status é PENDING, {@code false} caso contrário
     */
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }
    
    /**
     * Verifica se o pedido foi pago com sucesso.
     * 
     * @return {@code true} se status é PAID, {@code false} caso contrário
     */
    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }
    
    /**
     * Anexa o ID de pagamento ao pedido sem alterar o status.
     *
     * <p>Útil para cenários onde a cobrança foi criada no gateway mas o
     * pagamento ainda está pendente.</p>
     *
     * @param paymentId ID do pagamento no gateway externo
     */
    public void attachPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o pagamento está pendente (cobrança criada aguardando confirmação).
     *
     * @return {@code true} se status é PAYMENT_PENDING, {@code false} caso contrário
     */
    public boolean isPaymentPending() {
        return status == OrderStatus.PAYMENT_PENDING;
    }
    
    /**
     * Verifica se o pedido foi cancelado.
     * 
     * @return {@code true} se status é CANCELED, {@code false} caso contrário
     */
    public boolean isCanceled() {
        return status == OrderStatus.CANCELED;
    }
    
    /**
     * Verifica se o pagamento falhou.
     * 
     * @return {@code true} se status é PAYMENT_FAILED, {@code false} caso contrário
     */
    public boolean isPaymentFailed() {
        return status == OrderStatus.PAYMENT_FAILED;
    }
    
    /**
     * Atualiza o nível de risco do pedido.
     * 
     * <p>Usado após análise de risco ser concluída (via IA ou outro método).
     * Atualiza o timestamp de atualização automaticamente.</p>
     * 
     * @param riskLevel Novo nível de risco (LOW, HIGH, ou PENDING)
     */
    public void updateRiskLevel(RiskLevel riskLevel) {
        if (riskLevel == null) {
            throw new IllegalArgumentException("Risk level cannot be null");
        }
        this.riskLevel = riskLevel;
        this.updatedAt = LocalDateTime.now();
    }
}

