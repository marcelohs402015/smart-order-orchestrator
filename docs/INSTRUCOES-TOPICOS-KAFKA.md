# Instruções: Criação de Tópicos Kafka

> **📋 Guia para criar os tópicos necessários no Kafka antes da integração**

---

## 📌 Tópicos Necessários

Você precisa criar **4 tópicos** no seu Kafka para que a integração funcione corretamente:

### 1. **order-created**
- **Evento:** `OrderCreatedEvent`
- **Publicado quando:** Pedido é criado com sucesso (Step 1 da Saga)
- **Consumidores típicos:** Notification Service, Inventory Service, Analytics Service

### 2. **payment-processed**
- **Evento:** `PaymentProcessedEvent`
- **Publicado quando:** Pagamento é processado (Step 2 da Saga)
- **Consumidores típicos:** Inventory Service, Notification Service, Accounting Service

### 3. **saga-completed**
- **Evento:** `SagaCompletedEvent`
- **Publicado quando:** Saga completa com sucesso (todos os 3 steps concluídos)
- **Consumidores típicos:** Fulfillment Service, Notification Service, Analytics Service

### 4. **saga-failed**
- **Evento:** `SagaFailedEvent`
- **Publicado quando:** Saga falha e compensação é executada
- **Consumidores típicos:** Notification Service, Inventory Service, Alerting Service

---

## 🔧 Configuração Recomendada dos Tópicos

### Para Desenvolvimento/Testes:
```bash
# Partições: 3 (permite paralelismo)
# Replicação: 1 (desenvolvimento local)
# Retention: 7 dias
# Cleanup Policy: delete
```

### Para Produção:
```bash
# Partições: 6-12 (depende do volume)
# Replicação: 3 (alta disponibilidade)
# Retention: 30 dias (ou conforme política)
# Cleanup Policy: delete ou compact (se necessário)
```

---

## 📝 Comandos para Criar Tópicos

### Usando Kafka CLI (kafka-topics.sh):

```bash
# 1. order-created
kafka-topics --create \
  --bootstrap-server <seu-kafka-server>:9092 \
  --topic order-created \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete

# 2. payment-processed
kafka-topics --create \
  --bootstrap-server <seu-kafka-server>:9092 \
  --topic payment-processed \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete

# 3. saga-completed
kafka-topics --create \
  --bootstrap-server <seu-kafka-server>:9092 \
  --topic saga-completed \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete

# 4. saga-failed
kafka-topics --create \
  --bootstrap-server <seu-kafka-server>:9092 \
  --topic saga-failed \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete
```

### Usando Docker (se Kafka estiver em container):

```bash
# Acessar container do Kafka
docker exec -it <kafka-container-name> bash

# Criar tópicos
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --partitions 3 \
  --replication-factor 1

# Repetir para os outros 3 tópicos
```

### Usando Confluent Control Center (UI):

1. Acesse o Confluent Control Center
2. Vá em **Topics** > **Add a Topic**
3. Crie cada tópico com as configurações acima

### Usando Kafka Manager / Kafka UI:

1. Acesse a interface web
2. Vá em **Topics** > **Create Topic**
3. Configure cada tópico conforme especificado

---

## ✅ Verificar Tópicos Criados

### Listar todos os tópicos:
```bash
kafka-topics --list --bootstrap-server <seu-kafka-server>:9092
```

### Ver detalhes de um tópico específico:
```bash
kafka-topics --describe \
  --bootstrap-server <seu-kafka-server>:9092 \
  --topic order-created
```

### Verificar se tópicos existem:
```bash
# Deve retornar os 4 tópicos:
# order-created
# payment-processed
# saga-completed
# saga-failed
kafka-topics --list --bootstrap-server <seu-kafka-server>:9092 | grep -E "(order-created|payment-processed|saga-completed|saga-failed)"
```

---

## 🔄 Alternativa: Criação Automática

**Nota:** Se preferir, a aplicação pode criar os tópicos automaticamente usando `KafkaAdmin` (opcional no plano). Neste caso, você **não precisa criar manualmente**, mas é recomendado criar explicitamente para ter controle sobre as configurações.

---

## 📊 Estrutura de Dados dos Eventos

Todos os eventos serão serializados como **JSON** e incluirão os seguintes campos comuns:

```json
{
  "eventId": "uuid",
  "aggregateId": "uuid (Order ID)",
  "occurredAt": "2024-12-12T10:30:00",
  "eventType": "OrderCreated",
  "eventVersion": "1.0"
}
```

### Headers Kafka (Metadados):
- `eventId`: ID único do evento
- `aggregateId`: ID do pedido (Order ID)
- `eventType`: Tipo do evento (OrderCreated, PaymentProcessed, etc.)
- `eventVersion`: Versão do schema (1.0)
- `occurredAt`: Timestamp do evento

---

## 🎯 Resumo Rápido

**Tópicos a criar:**
1. ✅ `order-created`
2. ✅ `payment-processed`
3. ✅ `saga-completed`
4. ✅ `saga-failed`

**Configuração mínima:**
- Partições: 3
- Replicação: 1 (dev) / 3 (prod)
- Retention: 7 dias (dev) / 30 dias (prod)

**Após criar os tópicos:**
- Informe o endereço do Kafka: `KAFKA_BOOTSTRAP_SERVERS=<endereço>:9092`
- A aplicação se conectará automaticamente quando `MESSAGE_BROKER_TYPE=KAFKA`

---

## 🧪 Teste rápido de publicação/consumo

1) Suba a aplicação com Kafka:
```bash
export MESSAGE_BROKER_TYPE=KAFKA
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
mvn spring-boot:run
```

2) Crie um pedido (exemplo):
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId":"11111111-1111-1111-1111-111111111111",
    "customerName":"Teste",
    "customerEmail":"teste@example.com",
    "paymentMethod":"CREDIT_CARD",
    "currency":"BRL",
    "items":[{"productId":"22222222-2222-2222-2222-222222222222","productName":"Item","quantity":1,"unitPrice":100.0}]
  }'
```

3) Consuma eventos publicados:
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic order-created --from-beginning
```

4) Tópicos gerados automaticamente (KafkaAdmin):
- order-created
- payment-processed
- saga-completed
- saga-failed

5) Onde os tópicos são criados no código:
- `KafkaConfiguration` (`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/KafkaConfiguration.java`)
  - Beans `NewTopic` (order-created, payment-processed, saga-completed, saga-failed)
  - `KafkaAdmin` usa `spring.kafka.bootstrap-servers`

6) Publicação (Adapter):
- `KafkaEventPublisherAdapter` (`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/KafkaEventPublisherAdapter.java`)
  - Mapeia eventType → tópico
  - Headers: eventId, aggregateId, eventType, eventVersion, occurredAt

---

## 🌐 UI do Kafka (Observabilidade)

- A UI do Kafka roda em:
  - `http://localhost:8080/ui/clusters/Local-Apache/brokers`
- Permite:
  - Visualizar brokers, tópicos, consumer groups e mensagens
  - Navegar até:
    - **Clusters → Local-Apache → Topics** para ver `order-created`, `payment-processed`, `saga-completed`, `saga-failed`
    - **Consumer Groups** para monitorar consumo

Use essa UI como painel principal para validar:
- Se os tópicos foram criados
- Se as mensagens estão sendo publicadas corretamente pelos testes end-to-end

**📅 Documento criado em:** 12/12/2025  
**👨‍💻 Mantido por:** Marcelo Hernandes da Silva

