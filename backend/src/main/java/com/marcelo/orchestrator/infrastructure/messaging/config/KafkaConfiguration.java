package com.marcelo.orchestrator.infrastructure.messaging.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


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
    
    
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        log.info("Configuring KafkaAdmin with bootstrap servers: {}", bootstrapServers);
        
        return new KafkaAdmin(configs);
    }
    
    
    @Bean
    public NewTopic orderCreatedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", orderCreatedTopic);
        return TopicBuilder.name(orderCreatedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    
    @Bean
    public NewTopic paymentProcessedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", paymentProcessedTopic);
        return TopicBuilder.name(paymentProcessedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    
    @Bean
    public NewTopic sagaCompletedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", sagaCompletedTopic);
        return TopicBuilder.name(sagaCompletedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    
    @Bean
    public NewTopic sagaFailedTopic() {
        log.info("Creating topic: {} (partitions: 3, replication: 1)", sagaFailedTopic);
        return TopicBuilder.name(sagaFailedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    
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
    
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        log.info("Creating KafkaTemplate for event publishing");
        return new KafkaTemplate<>(producerFactory());
    }

    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        log.info("Configuring Kafka ConsumerFactory with auto-offset-reset=earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

