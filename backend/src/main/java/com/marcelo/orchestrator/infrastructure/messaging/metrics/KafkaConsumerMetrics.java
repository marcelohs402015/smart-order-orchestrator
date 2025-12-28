package com.marcelo.orchestrator.infrastructure.messaging.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class KafkaConsumerMetrics {

    private final MeterRegistry meterRegistry;

    public KafkaConsumerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private static final String METRIC_PREFIX = "kafka.consumer";
    private static final String TOPIC_TAG = "topic";
    private static final String EVENT_TYPE_TAG = "event_type";
    private static final String PARTITION_TAG = "partition";
    private static final String STATUS_TAG = "status";

    public void recordMessageConsumed(String topic, String eventType, int partition) {
        Counter.builder(METRIC_PREFIX + ".messages.total")
                .description("Total de mensagens Kafka consumidas")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .register(meterRegistry)
                .increment();

        Counter.builder(METRIC_PREFIX + ".messages.by_event_type")
                .description("Mensagens consumidas agrupadas por tipo de evento")
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .register(meterRegistry)
                .increment();

        Counter.builder(METRIC_PREFIX + ".messages.by_partition")
                .description("Mensagens consumidas por partição")
                .tag(TOPIC_TAG, topic)
                .tag(PARTITION_TAG, String.valueOf(partition))
                .register(meterRegistry)
                .increment();
    }

    public void recordProcessingTime(String topic, String eventType, Duration duration) {
        Timer.builder(METRIC_PREFIX + ".processing.duration")
                .description("Tempo de processamento de mensagens Kafka")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .register(meterRegistry)
                .record(duration);
    }

    public void recordSuccess(String topic, String eventType) {
        Counter.builder(METRIC_PREFIX + ".messages.processed")
                .description("Mensagens Kafka processadas com sucesso")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .tag(STATUS_TAG, "success")
                .register(meterRegistry)
                .increment();
    }

    public void recordError(String topic, String eventType, Throwable error) {
        Counter.builder(METRIC_PREFIX + ".messages.processed")
                .description("Mensagens Kafka com erro no processamento")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .tag(STATUS_TAG, "error")
                .tag("error_type", error.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
    }
}
