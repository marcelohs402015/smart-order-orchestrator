package com.marcelo.orchestrator.domain.model;

import java.util.Set;

/**
 * Enum que representa os possíveis estados de um pedido.
 * 
 * <p>Implementa o padrão <strong>State Machine</strong> para controlar transições de estado
 * de forma segura e garantir consistência do domínio.</p>
 * 
 * <h3>Por que State Machine no Enum?</h3>
 * <ul>
 *   <li><strong>Encapsulamento:</strong> Regras de transição ficam no próprio enum,
 *       não espalhadas em services ou controllers.</li>
 *   <li><strong>Type Safety:</strong> Compilador garante que apenas estados válidos existem.</li>
 *   <li><strong>Imutabilidade:</strong> Estados são constantes, não podem ser alterados em runtime.</li>
 *   <li><strong>Testabilidade:</strong> Fácil testar transições válidas e inválidas.</li>
 * </ul>
 * 
 * <h3>Fluxo de Estados:</h3>
 * <pre>
 * PENDING → PAID → (Análise de Risco)
 * PENDING → PAYMENT_FAILED → CANCELED (compensação via saga)
 * PENDING → CANCELED
 * </pre>
 * 
 * <h3>Benefícios desta Abordagem:</h3>
 * <ul>
 *   <li>Previne transições inválidas (ex: PAID → PENDING)</li>
 *   <li>Centraliza lógica de negócio no domínio</li>
 *   <li>Facilita manutenção e evolução</li>
 * </ul>
 * 
 * @author Marcelo
 */
public enum OrderStatus {
    
    /**
     * Pedido criado, aguardando processamento de pagamento.
     * Pode transicionar para: PAID, PAYMENT_FAILED, CANCELED
     */
    PENDING,
    
    /**
     * Pagamento confirmado com sucesso.
     * Estado final positivo - pedido será processado.
     */
    PAID,
    
    /**
     * Falha no processamento do pagamento.
     * Pode transicionar para: CANCELED (compensação via saga)
     * Estado final negativo - pedido não será processado.
     */
    PAYMENT_FAILED,
    
    /**
     * Pedido cancelado pelo cliente ou sistema.
     * Estado final - pedido não será processado.
     */
    CANCELED;
    
    /**
     * Retorna os estados permitidos para transição a partir deste estado.
     * 
     * <p>Implementa a lógica de State Machine de forma centralizada.
     * Esta abordagem evita referências circulares no construtor do enum.</p>
     * 
     * <p><strong>Nota sobre compensação:</strong> PAYMENT_FAILED pode transicionar para CANCELED
     * para permitir compensação em sagas quando o pagamento falha.</p>
     * 
     * @return Conjunto imutável de estados permitidos para transição
     */
    public Set<OrderStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> Set.of(PAID, PAYMENT_FAILED, CANCELED);
            case PAYMENT_FAILED -> Set.of(CANCELED); // Permite compensação via saga
            case PAID, CANCELED -> Set.of(); // Estados finais
        };
    }
    
    /**
     * Valida se é possível transicionar deste estado para o estado de destino.
     * 
     * <p>Implementa a validação de transição de estado, garantindo que apenas
     * transições válidas sejam permitidas. Isso previne estados inconsistentes
     * e garante a integridade do domínio.</p>
     * 
     * @param targetStatus Estado de destino desejado
     * @return {@code true} se a transição é permitida, {@code false} caso contrário
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        return getAllowedTransitions().contains(targetStatus);
    }
    
}

