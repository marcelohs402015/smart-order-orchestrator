package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.event.DomainEvent;

/**
 * Porta (Port) para publicação de eventos de domínio em message brokers.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) para publicação
 * de eventos em sistemas de mensageria (filas, tópicos, pub/sub).</p>
 * 
 * <h3>Padrão: Port (Hexagonal Architecture)</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na Infrastructure com diferentes message brokers</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes de Kafka, Pub/Sub, RabbitMQ, etc.</li>
 * </ul>
 * 
 * <h3>Por que Interface ao invés de Classe Concreta?</h3>
 * <ul>
 *   <li><strong>Dependency Inversion Principle (SOLID):</strong> Domínio não depende de implementação</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar para testes unitários</li>
 *   <li><strong>Flexibilidade:</strong> Trocar message broker sem alterar domínio</li>
 *   <li><strong>Múltiplas Implementações:</strong> Pode ter adaptadores para Kafka, Pub/Sub, RabbitMQ, etc.</li>
 * </ul>
 * 
 * <h3>Padrão: Event-Driven Architecture</h3>
 * <p>Este port permite implementar <strong>Event-Driven Architecture</strong>, onde:</p>
 * <ul>
 *   <li><strong>Desacoplamento:</strong> Produtores não conhecem consumidores</li>
 *   <li><strong>Escalabilidade:</strong> Múltiplos consumidores podem processar eventos</li>
 *   <li><strong>Resiliência:</strong> Eventos são persistidos, permitindo reprocessamento</li>
 *   <li><strong>Observabilidade:</strong> Eventos permitem rastreamento completo do fluxo</li>
 * </ul>
 * 
 * <h3>Uso no Saga Pattern:</h3>
 * <p>O Saga Orchestrator publica eventos em cada step, permitindo que outros serviços
 * sejam notificados sobre mudanças de estado sem acoplamento direto:</p>
 * <ul>
 *   <li><strong>OrderCreatedEvent:</strong> Publicado quando pedido é criado</li>
 *   <li><strong>PaymentProcessedEvent:</strong> Publicado quando pagamento é processado</li>
 *   <li><strong>RiskAnalyzedEvent:</strong> Publicado quando análise de risco é concluída</li>
 *   <li><strong>SagaCompletedEvent:</strong> Publicado quando saga completa com sucesso</li>
 *   <li><strong>SagaFailedEvent:</strong> Publicado quando saga falha</li>
 * </ul>
 * 
 * <h3>Exemplo de Consumidores:</h3>
 * <ul>
 *   <li><strong>Notification Service:</strong> Envia email/SMS quando pedido é criado</li>
 *   <li><strong>Inventory Service:</strong> Reserva estoque quando pagamento é processado</li>
 *   <li><strong>Analytics Service:</strong> Coleta métricas de todos os eventos</li>
 *   <li><strong>Audit Service:</strong> Registra todos os eventos para auditoria</li>
 * </ul>
 * 
 * @author Marcelo
 */
public interface EventPublisherPort {
    
    /**
     * Publica um evento de domínio no message broker.
     * 
     * <p>A implementação deve garantir:</p>
     * <ul>
     *   <li><strong>At-least-once delivery:</strong> Evento será entregue pelo menos uma vez</li>
     *   <li><strong>Idempotência:</strong> Consumidores devem lidar com eventos duplicados</li>
     *   <li><strong>Ordem:</strong> Eventos do mesmo aggregate devem manter ordem (se suportado pelo broker)</li>
     *   <li><strong>Resiliência:</strong> Falhas na publicação não devem quebrar o fluxo principal</li>
     * </ul>
     * 
     * <h3>Padrão: Fail-Safe</h3>
     * <p>Se a publicação falhar, deve logar o erro mas não lançar exceção, para não
     * interromper o fluxo principal da saga. O evento pode ser republicado posteriormente
     * via recovery service.</p>
     * 
     * @param event Evento de domínio a ser publicado
     * @param <T> Tipo do evento (deve implementar DomainEvent)
     */
    <T extends DomainEvent> void publish(T event);
    
    /**
     * Publica múltiplos eventos em batch (otimização).
     * 
     * <p>Útil quando múltiplos eventos precisam ser publicados juntos,
     * reduzindo overhead de rede e melhorando throughput.</p>
     * 
     * @param events Lista de eventos a serem publicados
     * @param <T> Tipo dos eventos (devem implementar DomainEvent)
     */
    <T extends DomainEvent> void publishBatch(java.util.List<T> events);
}

