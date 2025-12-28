# Observabilidade - Smart Order Orchestrator

Este diretório contém as configurações de observabilidade do projeto, incluindo Prometheus e Grafana para monitoramento do consumo Kafka.

## 📊 Arquitetura de Observabilidade

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │───► /actuator/prometheus
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Prometheus    │───► Coleta métricas a cada 15s
└─────────────────┘
         │
         ▼
┌─────────────────┐
│    Grafana      │───► Visualização e Dashboards
└─────────────────┘
```

## 🚀 Como Usar

### 1. Iniciar Infraestrutura de Observabilidade

```bash
# Iniciar Prometheus e Grafana
docker-compose up -d prometheus grafana
```

### 2. Acessar Dashboards

- **Grafana**: http://localhost:3000
  - Usuário: `admin`
  - Senha: `admin`
  
- **Prometheus**: http://localhost:9090

### 3. Verificar Métricas da Aplicação

A aplicação Spring Boot expõe métricas em:
- **Endpoint Prometheus**: http://localhost:8081/actuator/prometheus
- **Health Check**: http://localhost:8081/actuator/health

## 📈 Métricas Kafka Expostas

O componente `KafkaConsumerMetrics` coleta as seguintes métricas:

### Contadores (Counters)
- `kafka_consumer_messages_total_total`: Total de mensagens consumidas (por tópico e tipo de evento)
- `kafka_consumer_messages_by_event_type_total_total`: Mensagens agrupadas por tipo de evento
- `kafka_consumer_messages_by_partition_total_total`: Mensagens por partição
- `kafka_consumer_messages_processed_total_total`: Mensagens processadas (sucesso/erro)

### Histogramas (Timers)
- `kafka_consumer_processing_duration_seconds`: Tempo de processamento (latência)
  - Expõe percentis: p50, p95, p99
  - Permite análise de performance e identificação de gargalos

## 🎨 Dashboard Grafana

O dashboard `kafka-consumer-dashboard.json` inclui:

1. **Total de Mensagens Consumidas**: Taxa de consumo em tempo real
2. **Mensagens por Tópico**: Distribuição de mensagens por tópico Kafka
3. **Mensagens por Tipo de Evento**: Gráfico de pizza mostrando distribuição
4. **Tempo de Processamento**: Latência (p50, p95, p99) por tópico
5. **Mensagens por Partição**: Distribuição de carga entre partições
6. **Taxa de Sucesso vs Erro**: Monitoramento de erros no processamento

## 🔧 Configuração

### Prometheus (`prometheus.yml`)

- **Scrape Interval**: 15 segundos
- **Target**: `host.docker.internal:8081` (aplicação Spring Boot)
- **Metrics Path**: `/actuator/prometheus`

### Grafana

- **Data Source**: Configurado automaticamente via provisioning
- **Dashboards**: Carregados automaticamente do diretório `dashboards/`

## 🎯 Casos de Uso

Este sistema de observabilidade demonstra:

1. **Capacidade de Observabilidade**: Monitoramento completo do consumo Kafka
2. **Gerenciamento de Sistemas Distribuídos**: Visualização de métricas em tempo real
3. **Identificação de Problemas**: Alertas e métricas de erro
4. **Otimização de Performance**: Análise de latência e throughput

## 📝 Notas Técnicas

- **Separação de Concerns**: Métricas isoladas em componente dedicado (`KafkaConsumerMetrics`)
- **SOLID**: Componente segue princípios de Single Responsibility e Dependency Inversion
- **Micrometer**: Uso de Micrometer para abstração de métricas (compatível com Prometheus)
- **Spring Boot Actuator**: Endpoint `/actuator/prometheus` expõe métricas no formato Prometheus

## 🔍 Troubleshooting

### Prometheus não consegue coletar métricas

1. Verifique se a aplicação está rodando: `http://localhost:8081/actuator/prometheus`
2. No Windows, use `host.docker.internal` para acessar o host do Docker
3. Verifique os logs do Prometheus: `docker logs smartorder-prometheus`

### Grafana não mostra dados

1. Verifique se o Prometheus está configurado como data source
2. Verifique se as métricas estão sendo coletadas no Prometheus
3. Verifique os nomes das métricas no dashboard (podem variar conforme versão do Micrometer)

