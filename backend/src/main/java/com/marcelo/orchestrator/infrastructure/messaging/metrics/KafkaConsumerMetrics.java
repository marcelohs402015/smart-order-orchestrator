package com.marcelo.orchestrator.infrastructure.messaging.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Componente responsável por coletar métricas customizadas do consumo Kafka.
 *
 * <p>Separação de Concerns: Esta classe isola toda a lógica de métricas do consumo Kafka,
 * mantendo o listener focado apenas em processar mensagens. Segue o princípio de
 * <strong>Single Responsibility</strong> do SOLID.</p>
 *
 * <h3>Padrão: Metrics Collector</h3>
 * <ul>
 *   <li><strong>Separação:</strong> Métricas separadas da lógica de negócio</li>
 *   <li><strong>Observabilidade:</strong> Expõe métricas via Micrometer/Prometheus</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar em testes unitários</li>
 * </ul>
 *
 * <h3>Métricas Expostas:</h3>
 * <ul>
 *   <li><strong>kafka.consumer.messages.total:</strong> Total de mensagens consumidas (por tópico)</li>
 *   <li><strong>kafka.consumer.processing.duration:</strong> Tempo de processamento (latência)</li>
 *   <li><strong>kafka.consumer.messages.by_event_type:</strong> Mensagens agrupadas por tipo de evento</li>
 * </ul>
 *
 * <h3>Alinhamento com SOLID:</h3>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Apenas coleta de métricas Kafka</li>
 *   <li><strong>Dependency Inversion:</strong> Depende de abstração (MeterRegistry), não implementação</li>
 *   <li><strong>Open/Closed:</strong> Fácil adicionar novas métricas sem modificar código existente</li>
 * </ul>
 *
 * @author Marcelo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerMetrics {

    private final MeterRegistry meterRegistry;

    private static final String METRIC_PREFIX = "kafka.consumer";
    private static final String TOPIC_TAG = "topic";
    private static final String EVENT_TYPE_TAG = "event_type";
    private static final String PARTITION_TAG = "partition";
    private static final String STATUS_TAG = "status";

    /**
     * Registra o consumo de uma mensagem Kafka.
     *
     * <p>Incrementa contadores para:
     * - Total de mensagens por tópico
     * - Mensagens por tipo de evento
     * - Mensagens por tópico e partição</p>
     *
     * @param topic     Nome do tópico Kafka
     * @param eventType Tipo do evento (ex: OrderCreatedEvent)
     * @param partition Partição da qual a mensagem foi consumida
     */
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

        log.debug("Recorded Kafka message consumption: topic={}, eventType={}, partition={}", 
                topic, eventType, partition);
    }

    /**
     * Registra o tempo de processamento de uma mensagem Kafka.
     *
     * <p>Usa Timer do Micrometer para coletar latência, percentis e taxa de processamento.
     * Essas métricas são essenciais para identificar gargalos e otimizar performance.</p>
     *
     * @param topic     Nome do tópico Kafka
     * @param eventType Tipo do evento
     * @param duration  Duração do processamento
     */
    public void recordProcessingTime(String topic, String eventType, Duration duration) {
        Timer.builder(METRIC_PREFIX + ".processing.duration")
                .description("Tempo de processamento de mensagens Kafka")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .register(meterRegistry)
                .record(duration);

        log.debug("Recorded Kafka processing time: topic={}, eventType={}, duration={}ms", 
                topic, eventType, duration.toMillis());
    }

    /**
     * Registra uma mensagem processada com sucesso.
     *
     * @param topic     Nome do tópico Kafka
     * @param eventType Tipo do evento
     */
    public void recordSuccess(String topic, String eventType) {
        Counter.builder(METRIC_PREFIX + ".messages.processed")
                .description("Mensagens Kafka processadas com sucesso")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .tag(STATUS_TAG, "success")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Registra uma falha no processamento de mensagem.
     *
     * @param topic     Nome do tópico Kafka
     * @param eventType Tipo do evento
     * @param error     Exceção ocorrida
     */
    public void recordError(String topic, String eventType, Throwable error) {
        Counter.builder(METRIC_PREFIX + ".messages.processed")
                .description("Mensagens Kafka com erro no processamento")
                .tag(TOPIC_TAG, topic)
                .tag(EVENT_TYPE_TAG, eventType != null ? eventType : "unknown")
                .tag(STATUS_TAG, "error")
                .tag("error_type", error.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();

        log.warn("Recorded Kafka processing error: topic={}, eventType={}, error={}", 
                topic, eventType, error.getMessage());
    }
}

