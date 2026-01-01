package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.AbstractIntegrationTest;
import com.marcelo.orchestrator.domain.event.saga.OrderCreatedEvent;
import com.marcelo.orchestrator.domain.event.saga.PaymentProcessedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("KafkaEventPublisherAdapter Integration Tests")
class KafkaEventPublisherAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaEventPublisherAdapter kafkaEventPublisherAdapter;

    private BlockingQueue<ConsumerRecord<String, Object>> records;
    private KafkaMessageListenerContainer<String, Object> container;

    @BeforeEach
    void setUp() {
        records = new LinkedBlockingQueue<>();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);

        DefaultKafkaConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties("order-created", "payment-processed");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, Object>) records::add);
        container.start();

        ContainerTestUtils.waitForAssignment(container, 2);
    }

    @Test
    @DisplayName("Should publish OrderCreatedEvent to Kafka topic")
    void shouldPublishOrderCreatedEvent() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .customerId(UUID.randomUUID())
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .totalAmount(BigDecimal.valueOf(100.00))
                .currency("BRL")
                .sagaId(UUID.randomUUID())
                .build();

        kafkaEventPublisherAdapter.publish(event);

        ConsumerRecord<String, Object> record = records.poll(10, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.topic()).isEqualTo("order-created");
        assertThat(record.key()).isEqualTo(event.getAggregateId().toString());

        assertThat(record.headers().lastHeader("eventId")).isNotNull();
        assertThat(record.headers().lastHeader("eventType")).isNotNull();
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("OrderCreated");
    }

    @Test
    @DisplayName("Should publish PaymentProcessedEvent to Kafka topic")
    void shouldPublishPaymentProcessedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(orderId)
                .occurredAt(LocalDateTime.now())
                .orderId(orderId)
                .paymentId("PAY-123456")
                .amount(BigDecimal.valueOf(200.00))
                .currency("BRL")
                .paymentStatus("PAID")
                .sagaId(UUID.randomUUID())
                .build();

        kafkaEventPublisherAdapter.publish(event);

        ConsumerRecord<String, Object> record = records.poll(10, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.topic()).isEqualTo("payment-processed");
        assertThat(record.key()).isEqualTo(orderId.toString());

        assertThat(record.headers().lastHeader("eventType")).isNotNull();
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("PaymentProcessed");
    }

    @Test
    @DisplayName("Should publish multiple events in batch")
    void shouldPublishBatchOfEvents() {
        OrderCreatedEvent event1 = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .orderNumber("ORD-1")
                .customerId(UUID.randomUUID())
                .customerName("Customer 1")
                .customerEmail("customer1@example.com")
                .totalAmount(BigDecimal.valueOf(100.00))
                .currency("BRL")
                .sagaId(UUID.randomUUID())
                .build();

        OrderCreatedEvent event2 = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .orderNumber("ORD-2")
                .customerId(UUID.randomUUID())
                .customerName("Customer 2")
                .customerEmail("customer2@example.com")
                .totalAmount(BigDecimal.valueOf(200.00))
                .currency("BRL")
                .sagaId(UUID.randomUUID())
                .build();

        kafkaEventPublisherAdapter.publishBatch(java.util.List.of(event1, event2));

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> records.size() >= 2);

        assertThat(records).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should use aggregateId as message key")
    void shouldUseAggregateIdAsMessageKey() throws Exception {
        UUID aggregateId = UUID.randomUUID();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(aggregateId)
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .orderNumber("ORD-KEY-TEST")
                .customerId(UUID.randomUUID())
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .totalAmount(BigDecimal.valueOf(150.00))
                .currency("BRL")
                .sagaId(UUID.randomUUID())
                .build();

        kafkaEventPublisherAdapter.publish(event);

        ConsumerRecord<String, Object> record = records.poll(10, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(aggregateId.toString());
    }

    @Test
    @DisplayName("Should include all required headers in Kafka message")
    void shouldIncludeAllRequiredHeaders() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .orderNumber("ORD-HEADERS-TEST")
                .customerId(UUID.randomUUID())
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .totalAmount(BigDecimal.valueOf(100.00))
                .currency("BRL")
                .sagaId(UUID.randomUUID())
                .build();

        kafkaEventPublisherAdapter.publish(event);

        ConsumerRecord<String, Object> record = records.poll(10, TimeUnit.SECONDS);

        assertThat(record).isNotNull();
        assertThat(record.headers().lastHeader("eventId")).isNotNull();
        assertThat(record.headers().lastHeader("aggregateId")).isNotNull();
        assertThat(record.headers().lastHeader("eventType")).isNotNull();
        assertThat(record.headers().lastHeader("eventVersion")).isNotNull();
        assertThat(record.headers().lastHeader("occurredAt")).isNotNull();

        assertThat(new String(record.headers().lastHeader("eventVersion").value())).isEqualTo("1.0");
    }
}
