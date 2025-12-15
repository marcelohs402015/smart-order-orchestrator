package com.marcelo.orchestrator.infrastructure.messaging.adapter;

import com.marcelo.orchestrator.domain.event.DomainEvent;
import com.marcelo.orchestrator.domain.event.saga.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para KafkaEventPublisherAdapter.
 * 
 * <p>Testa a publicação de eventos no Kafka, garantindo que:</p>
 * <ul>
 *   <li>Eventos são publicados no tópico correto</li>
 *   <li>Headers Kafka são incluídos corretamente</li>
 *   <li>Fail-safe funciona (não lança exceção em caso de erro)</li>
 *   <li>Mapeamento de tipos de eventos para tópicos está correto</li>
 * </ul>
 * 
 * <p>Padrão: Unit Testing - testa apenas a lógica do adapter, mockando KafkaTemplate.</p>
 * 
 * @author Marcelo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisherAdapter Tests")
class KafkaEventPublisherAdapterTest {
    
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @InjectMocks
    private KafkaEventPublisherAdapter adapter;
    
    private UUID orderId;
    private UUID sagaId;
    private OrderCreatedEvent event;
    
    @BeforeEach
    void setUp() {
        // Configurar tópicos via reflection (simula @Value injection)
        ReflectionTestUtils.setField(adapter, "orderCreatedTopic", "order-created");
        ReflectionTestUtils.setField(adapter, "paymentProcessedTopic", "payment-processed");
        ReflectionTestUtils.setField(adapter, "sagaCompletedTopic", "saga-completed");
        ReflectionTestUtils.setField(adapter, "sagaFailedTopic", "saga-failed");
        
        orderId = UUID.randomUUID();
        sagaId = UUID.randomUUID();
        
        event = OrderCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(orderId)
            .occurredAt(LocalDateTime.now())
            .orderId(orderId)
            .orderNumber("ORD-1234567890")
            .customerId(UUID.randomUUID())
            .customerName("Test Customer")
            .customerEmail("test@example.com")
            .totalAmount(new BigDecimal("100.00"))
            .currency("BRL")
            .sagaId(sagaId)
            .build();
    }
    
    @Test
    @DisplayName("Deve publicar evento único com sucesso")
    void shouldPublishEventSuccessfully() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        // Act
        adapter.publish(event);
        
        // Assert
        ArgumentCaptor<Message<Object>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        
        Message<Object> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo("order-created");
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo(orderId.toString());
        assertThat(sentMessage.getHeaders().get("eventId")).isEqualTo(event.getEventId().toString());
        assertThat(sentMessage.getHeaders().get("aggregateId")).isEqualTo(orderId.toString());
        assertThat(sentMessage.getHeaders().get("eventType")).isEqualTo("OrderCreated");
        assertThat(sentMessage.getHeaders().get("eventVersion")).isEqualTo("1.0");
        assertThat(sentMessage.getPayload()).isEqualTo(event);
    }
    
    @Test
    @DisplayName("Deve usar aggregateId como key da mensagem")
    void shouldUseAggregateIdAsKey() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        // Act
        adapter.publish(event);
        
        // Assert
        ArgumentCaptor<Message<Object>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<Object> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.KEY))
            .isEqualTo(orderId.toString());
    }
    
    @Test
    @DisplayName("Deve mapear corretamente tipo de evento para tópico")
    void shouldMapEventTypeToCorrectTopic() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        // Act
        adapter.publish(event);
        
        // Assert
        ArgumentCaptor<Message<Object>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<Object> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.TOPIC))
            .isEqualTo("order-created");
    }
    
    @Test
    @DisplayName("Deve incluir todos os headers Kafka necessários")
    void shouldIncludeAllKafkaHeaders() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        // Act
        adapter.publish(event);
        
        // Assert
        ArgumentCaptor<Message<Object>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<Object> sentMessage = messageCaptor.getValue();
        var headers = sentMessage.getHeaders();
        
        assertThat(headers.get("eventId")).isNotNull();
        assertThat(headers.get("aggregateId")).isNotNull();
        assertThat(headers.get("eventType")).isNotNull();
        assertThat(headers.get("eventVersion")).isNotNull();
        assertThat(headers.get("occurredAt")).isNotNull();
    }
    
    @Test
    @DisplayName("Deve logar erro mas não lançar exceção em caso de falha (fail-safe)")
    void shouldNotThrowExceptionOnFailure() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class)))
            .thenThrow(new RuntimeException("Kafka connection failed"));
        
        // Act & Assert - não deve lançar exceção
        adapter.publish(event);
        
        // Verificar que tentou publicar
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }
    
    @Test
    @DisplayName("Deve publicar batch de eventos")
    void shouldPublishBatchOfEvents() {
        // Arrange
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        var event2 = OrderCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .occurredAt(LocalDateTime.now())
            .orderId(UUID.randomUUID())
            .orderNumber("ORD-9876543210")
            .customerId(UUID.randomUUID())
            .customerName("Another Customer")
            .customerEmail("another@example.com")
            .totalAmount(new BigDecimal("200.00"))
            .currency("BRL")
            .sagaId(UUID.randomUUID())
            .build();
        
        // Act
        adapter.publishBatch(java.util.List.of(event, event2));
        
        // Assert
        verify(kafkaTemplate, times(2)).send(any(Message.class));
    }
    
    @Test
    @DisplayName("Deve usar eventId como key quando aggregateId for null")
    void shouldUseEventIdAsKeyWhenAggregateIdIsNull() {
        // Arrange
        var eventWithoutAggregateId = OrderCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .aggregateId(null) // null aggregateId
            .occurredAt(LocalDateTime.now())
            .orderId(orderId)
            .orderNumber("ORD-1234567890")
            .customerId(UUID.randomUUID())
            .customerName("Test Customer")
            .customerEmail("test@example.com")
            .totalAmount(new BigDecimal("100.00"))
            .currency("BRL")
            .sagaId(sagaId)
            .build();
        
        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);
        
        // Act
        adapter.publish(eventWithoutAggregateId);
        
        // Assert
        ArgumentCaptor<Message<Object>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        
        Message<Object> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.KEY))
            .isEqualTo(eventWithoutAggregateId.getEventId().toString());
    }
}

