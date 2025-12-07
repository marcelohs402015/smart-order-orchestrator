package com.marcelo.orchestrator.infrastructure.messaging.factory;

import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.InMemoryEventPublisherAdapter;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.KafkaEventPublisherAdapter;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.PubSubEventPublisherAdapter;
import com.marcelo.orchestrator.infrastructure.messaging.adapter.RabbitMqEventPublisherAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory para criar instâncias de EventPublisherPort baseado na configuração.
 * 
 * <p>Implementa o padrão <strong>Factory Pattern</strong> para permitir que o sistema
 * use diferentes message brokers (Kafka, Pub/Sub, RabbitMQ, etc.) sem alterar o código
 * que usa o EventPublisherPort.</p>
 * 
 * <h3>Padrão: Factory Pattern</h3>
 * <ul>
 *   <li><strong>Encapsula Criação:</strong> Centraliza a lógica de criação de objetos</li>
 *   <li><strong>Flexibilidade:</strong> Permite trocar implementação via configuração</li>
 *   <li><strong>Extensibilidade:</strong> Fácil adicionar novos tipos de message brokers</li>
 *   <li><strong>Desacoplamento:</strong> Clientes não conhecem qual implementação está sendo usada</li>
 * </ul>
 * 
 * <h3>Por que Factory Pattern aqui?</h3>
 * <ul>
 *   <li><strong>Múltiplas Implementações:</strong> Sistema pode usar Kafka, Pub/Sub, RabbitMQ, etc.</li>
 *   <li><strong>Configuração Dinâmica:</strong> Escolha do broker via propriedade (app.message.broker.type)</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar ou usar implementação in-memory em testes</li>
 *   <li><strong>Evolução:</strong> Pode migrar de um broker para outro sem alterar código cliente</li>
 * </ul>
 * 
 * <h3>Tipos Suportados:</h3>
 * <ul>
 *   <li><strong>IN_MEMORY:</strong> Implementação em memória (para testes e desenvolvimento)</li>
 *   <li><strong>KAFKA:</strong> Apache Kafka (alta performance, escalável)</li>
 *   <li><strong>PUBSUB:</strong> Google Cloud Pub/Sub (GCP nativo, serverless)</li>
 *   <li><strong>RABBITMQ:</strong> RabbitMQ (AMQP, fácil de usar)</li>
 *   <li><strong>SQS:</strong> AWS SQS (AWS nativo, serverless)</li>
 * </ul>
 * 
 * <h3>Uso:</h3>
 * <pre>{@code
 * // Configuração em application.yml:
 * message:
 *   broker:
 *     type: KAFKA  # ou PUBSUB, RABBITMQ, etc.
 * 
 * // Spring injeta automaticamente:
 * @Autowired
 * private EventPublisherPort eventPublisher; // Usa implementação correta
 * }</pre>
 * 
 * <h3>Extensibilidade:</h3>
 * <p>Para adicionar novo message broker:</p>
 * <ol>
 *   <li>Criar adapter implementando EventPublisherPort (ex: SnsEventPublisherAdapter)</li>
 *   <li>Adicionar tipo no enum MessageBrokerType</li>
 *   <li>Adicionar case no método create()</li>
 *   <li>Configurar Spring Bean para o novo adapter</li>
 * </ol>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
public class EventPublisherFactory {
    
    /**
     * Tipo de message broker a ser usado.
     * 
     * <p>Configurado via propriedade: {@code app.message.broker.type}</p>
     * <p>Valores possíveis: IN_MEMORY, KAFKA, PUBSUB, RABBITMQ, SQS</p>
     */
    @Value("${app.message.broker.type:IN_MEMORY}")
    private String brokerType;
    
    private final InMemoryEventPublisherAdapter inMemoryAdapter;
    private final KafkaEventPublisherAdapter kafkaAdapter;
    private final PubSubEventPublisherAdapter pubSubAdapter;
    private final RabbitMqEventPublisherAdapter rabbitMqAdapter;
    
    /**
     * Construtor com injeção de dependências de todos os adapters.
     * 
     * <p>Padrão: Dependency Injection (Spring) - permite que Spring gerencie
     * o ciclo de vida dos adapters e injete apenas o necessário baseado na configuração.</p>
     */
    public EventPublisherFactory(
            InMemoryEventPublisherAdapter inMemoryAdapter,
            KafkaEventPublisherAdapter kafkaAdapter,
            PubSubEventPublisherAdapter pubSubAdapter,
            RabbitMqEventPublisherAdapter rabbitMqAdapter) {
        this.inMemoryAdapter = inMemoryAdapter;
        this.kafkaAdapter = kafkaAdapter;
        this.pubSubAdapter = pubSubAdapter;
        this.rabbitMqAdapter = rabbitMqAdapter;
    }
    
    /**
     * Cria instância de EventPublisherPort baseado na configuração.
     * 
     * <p>Padrão: Factory Method - encapsula a lógica de criação e retorna
     * a implementação apropriada baseada no tipo configurado.</p>
     * 
     * @return Instância de EventPublisherPort configurada
     * @throws IllegalArgumentException se tipo não for suportado
     */
    public EventPublisherPort create() {
        MessageBrokerType type = MessageBrokerType.fromString(brokerType);
        
        log.info("Creating EventPublisherPort for broker type: {}", type);
        
        return switch (type) {
            case IN_MEMORY -> {
                log.debug("Using InMemoryEventPublisherAdapter (for tests/dev)");
                yield inMemoryAdapter;
            }
            case KAFKA -> {
                log.debug("Using KafkaEventPublisherAdapter");
                yield kafkaAdapter;
            }
            case PUBSUB -> {
                log.debug("Using PubSubEventPublisherAdapter (GCP)");
                yield pubSubAdapter;
            }
            case RABBITMQ -> {
                log.debug("Using RabbitMqEventPublisherAdapter");
                yield rabbitMqAdapter;
            }
            case SQS -> {
                log.warn("SQS adapter not yet implemented, falling back to IN_MEMORY");
                yield inMemoryAdapter;
            }
        };
    }
    
    /**
     * Enum com tipos de message brokers suportados.
     * 
     * <p>Padrão: Type-Safe Enum - garante que apenas valores válidos sejam usados,
     * prevenindo erros de configuração.</p>
     */
    public enum MessageBrokerType {
        IN_MEMORY("IN_MEMORY"),
        KAFKA("KAFKA"),
        PUBSUB("PUBSUB"),
        RABBITMQ("RABBITMQ"),
        SQS("SQS");
        
        private final String value;
        
        MessageBrokerType(String value) {
            this.value = value;
        }
        
        public static MessageBrokerType fromString(String value) {
            for (MessageBrokerType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported message broker type: " + value);
        }
    }
}

