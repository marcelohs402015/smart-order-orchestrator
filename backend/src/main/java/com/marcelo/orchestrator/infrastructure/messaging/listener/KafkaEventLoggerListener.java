package com.marcelo.orchestrator.infrastructure.messaging.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener simples para logar mensagens recebidas dos tópicos Kafka.
 *
 * <p>Objetivo principal: demonstrar consumo de mensagens Kafka pela aplicação,
 * exibindo no console o conteúdo e metadados das mensagens.</p>
 *
 * <h3>Padrão: Observer / Event Listener</h3>
 * <ul>
 *   <li><strong>Listener:</strong> Esta classe observa eventos publicados nos tópicos Kafka</li>
 *   <li><strong>Side-effect:</strong> Apenas logging (sem lógica de negócio)</li>
 *   <li><strong>Separação:</strong> Mantém consumo para observabilidade separado do domínio</li>
 * </ul>
 *
 * <h3>Configuração de Offset:</h3>
 * <p>O consumer é configurado com <code>auto-offset-reset=earliest</code> no
 * <code>application.yml</code>, garantindo que, ao iniciar um novo consumer group,
 * as mensagens mais antigas sejam lidas primeiro.</p>
 *
 * <h3>Boas práticas:</h3>
 * <ul>
 *   <li>Uso apenas para logging / demo (não acopla lógica de negócio)</li>
 *   <li>Facilita demonstração em entrevistas e testes end-to-end</li>
 * </ul>
 *
 * @author Marcelo
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.message.broker.type", havingValue = "KAFKA")
public class KafkaEventLoggerListener {

    /**
     * Listener que consome eventos de todos os tópicos da saga e loga no console.
     *
     * <p>Consome como String (JSON bruto) para simplificar e focar na demonstração
     * de consumo. A responsabilidade de interpretar o JSON fica para potenciais
     * consumidores especializados em outros serviços.</p>
     *
     * @param record    registro completo recebido do Kafka (para debug avançado)
     * @param payload   corpo da mensagem (JSON do DomainEvent)
     * @param topic     nome do tópico
     * @param key       chave da mensagem (aggregateId ou eventId)
     */
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
    }
}


