package com.marcelo.orchestrator.infrastructure.messaging.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Apache Kafka para publicação de eventos de domínio.
 * 
 * <p>Implementa o padrão <strong>Configuration</strong> do Spring, centralizando
 * toda a configuração relacionada ao Kafka em um único lugar.</p>
 * 
 * <h3>Padrão: Configuration Class</h3>
 * <ul>
 *   <li><strong>Centralização:</strong> Toda configuração Kafka em um único lugar</li>
 *   <li><strong>Separação de Responsabilidades:</strong> Configuração separada da lógica de negócio</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar ou substituir em testes</li>
 * </ul>
 * 
 * <h3>Por que Bean Condicional?</h3>
 * <p>Usa {@code @ConditionalOnProperty} para criar beans apenas quando Kafka está configurado.
 * Isso permite que a aplicação funcione sem Kafka (usando IN_MEMORY), seguindo o princípio
 * de <strong>Fail-Safe</strong> e <strong>Dependency Inversion</strong>.</p>
 * 
 * <h3>KafkaAdmin - Criação Automática de Tópicos</h3>
 * <p>KafkaAdmin cria tópicos automaticamente na inicialização da aplicação se não existirem.
 * Isso garante que os tópicos necessários estejam disponíveis sem intervenção manual.</p>
 * 
 * <h3>Configurações Importantes:</h3>
 * <ul>
 *   <li><strong>acks=all:</strong> Garante durabilidade (todas as réplicas confirmam)</li>
 *   <li><strong>idempotence=true:</strong> Garante idempotência do producer</li>
 *   <li><strong>max.in.flight.requests=1:</strong> Garante ordem de mensagens (com idempotence)</li>
 *   <li><strong>retries=3:</strong> Retenta em caso de falha transitória</li>
 * </ul>
 * 
 * <h3>Alinhamento com SOLID:</h3>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Apenas configuração Kafka</li>
 *   <li><strong>Dependency Inversion:</strong> Configuração via properties, não hardcoded</li>
 *   <li><strong>Open/Closed:</strong> Fácil adicionar novos tópicos sem modificar código existente</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "KAFKA")
@RequiredArgsConstructor
public class KafkaConfiguration {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${app.message.broker.kafka.topics.order-created}")
    private String orderCreatedTopic;
    
    @Value("${app.message.broker.kafka.topics.payment-processed}")
    private String paymentProcessedTopic;
    
    @Value("${app.message.broker.kafka.topics.saga-completed}")
    private String sagaCompletedTopic;
    
    @Value("${app.message.broker.kafka.topics.saga-failed}")
    private String sagaFailedTopic;
    
    /**
     * Configura KafkaAdmin para gerenciar tópicos.
     * 
     * <p>KafkaAdmin permite criar, listar e deletar tópicos programaticamente.
     * Usado para garantir que os tópicos necessários existam na inicialização.</p>
     * 
     * <p>Padrão: Infrastructure Configuration - configuração de infraestrutura
     * separada da lógica de negócio (Hexagonal Architecture).</p>
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        log.info("Configuring KafkaAdmin with bootstrap servers: {}", bootstrapServers);
        
        return new KafkaAdmin(configs);
    }
    
    /**
     * Cria tópico para eventos de pedido criado.
     * 
     * <p>Configuração recomendada para desenvolvimento:
     * - Partições: 3 (permite paralelismo)
     * - Replicação: 1 (desenvolvimento local)</p>
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", orderCreatedTopic);
        return TopicBuilder.name(orderCreatedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    /**
     * Cria tópico para eventos de pagamento processado.
     */
    @Bean
    public NewTopic paymentProcessedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", paymentProcessedTopic);
        return TopicBuilder.name(paymentProcessedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    /**
     * Cria tópico para eventos de saga completa.
     */
    @Bean
    public NewTopic sagaCompletedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", sagaCompletedTopic);
        return TopicBuilder.name(sagaCompletedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    /**
     * Cria tópico para eventos de saga falhada.
     */
    @Bean
    public NewTopic sagaFailedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", sagaFailedTopic);
        return TopicBuilder.name(sagaFailedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    /**
     * Configura ProducerFactory para criar producers Kafka.
     * 
     * <p>ProducerFactory encapsula a configuração do producer, permitindo
     * criar múltiplos KafkaTemplate com a mesma configuração base.</p>
     * 
     * <p>Configurações importantes:</p>
     * <ul>
     *   <li><strong>Key Serializer:</strong> String (usamos Order ID como key)</li>
     *   <li><strong>Value Serializer:</strong> JSON (Jackson serializa DomainEvent)</li>
     *   <li><strong>Acks:</strong> all (garante durabilidade)</li>
     *   <li><strong>Idempotence:</strong> true (garante idempotência)</li>
     * </ul>
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        
        log.info("Configuring Kafka ProducerFactory with idempotence enabled");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Cria KafkaTemplate para publicação de eventos.
     * 
     * <p>KafkaTemplate é o componente principal usado pelo KafkaEventPublisherAdapter
     * para publicar eventos no Kafka. É thread-safe e pode ser usado por múltiplas threads.</p>
     * 
     * <p>Padrão: Template Method - KafkaTemplate encapsula a complexidade de publicação,
     * fornecendo uma API simples e limpa para o adapter.</p>
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        log.info("Creating KafkaTemplate for event publishing");
        return new KafkaTemplate<>(producerFactory());
    }
}

