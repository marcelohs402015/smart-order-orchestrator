package com.marcelo.orchestrator.infrastructure.messaging.listener;

import com.marcelo.orchestrator.infrastructure.messaging.metrics.KafkaConsumerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "KAFKA")
@RequiredArgsConstructor
public class KafkaEventLoggerListener {

    private final KafkaConsumerMetrics metrics;

    @KafkaListener(
        topics = {
            "${app.message.broker.kafka.topics.order-created}",
            "${app.message.broker.kafka.topics.payment-processed}",
            "${app.message.broker.kafka.topics.saga-completed}",
            "${app.message.broker.kafka.topics.saga-failed}"
        },
        groupId = "order-events-logger"
    )
    public void logEvent(
            ConsumerRecord<String, String> record,
            @Payload String payload,
            @Header(name = "eventType", required = false) String eventType,
            @Header(name = "eventId", required = false) String eventId,
            @Header(name = "aggregateId", required = false) String aggregateId) {

        final Instant startTime = Instant.now();

        try {
            metrics.recordMessageConsumed(
                    record.topic(),
                    eventType,
                    record.partition()
            );

            log.info(
                "KAFKA CONSUMER - topic={}, partition={}, offset={}, key={}, eventType={}, eventId={}, aggregateId={}, payload={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                eventType,
                eventId,
                aggregateId,
                payload
            );

            final Duration processingTime = Duration.between(startTime, Instant.now());
            metrics.recordProcessingTime(record.topic(), eventType, processingTime);
            metrics.recordSuccess(record.topic(), eventType);

        } catch (Exception e) {
            metrics.recordError(record.topic(), eventType, e);
            log.error("Error processing Kafka message: topic={}, eventType={}, error={}", 
                    record.topic(), eventType, e.getMessage(), e);
            throw e;
        }
    }
}
