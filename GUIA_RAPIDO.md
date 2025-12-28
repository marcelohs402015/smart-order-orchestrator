# 🚀 Guia Rápido - Smart Order Orchestrator

Guia passo a passo para subir o sistema completo e ver funcionando.

## ⚡ Início Rápido (5 minutos)

### 1️⃣ Iniciar Infraestrutura

```bash
# Na raiz do projeto
docker-compose up -d
```

Isso inicia:
- ✅ PostgreSQL (banco de dados)
- ✅ Prometheus (coleta de métricas)
- ✅ Grafana (dashboards visuais)

### 2️⃣ Configurar Variáveis de Ambiente

**Windows (PowerShell):**
```powershell
cd backend/src/main/resources/variaveis
. .\environment.ps1
```

**Linux/Mac:**
```bash
cd backend/src/main/resources/variaveis
source environment.sh
```

### 3️⃣ Iniciar Aplicação Spring Boot

```bash
cd backend
mvn spring-boot:run
```

Aguarde a mensagem: `Started OrchestratorApplication`

### 4️⃣ Acessar Dashboards

Agora você pode acessar:

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API REST** | http://localhost:8081/api/v1/orders | - |
| **Swagger UI** | http://localhost:8081/swagger-ui/index.html | - |
| **Health Check** | http://localhost:8081/actuator/health | - |
| **Métricas Prometheus** | http://localhost:8081/actuator/prometheus | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |

## 📊 Ver Observabilidade em Ação

### Passo 1: Acessar Grafana
1. Abra http://localhost:3000
2. Login: `admin` / `admin`
3. Vá em **Dashboards** → **Kafka Consumer Metrics**

### Passo 2: Gerar Tráfego Kafka

Crie alguns pedidos para gerar eventos Kafka:

**Via Swagger:**
1. Acesse http://localhost:8081/swagger-ui/index.html
2. Use o endpoint `POST /api/v1/orders`
3. Exemplo de body:
```json
{
  "customerId": "customer-123",
  "items": [
    {
      "productId": "prod-1",
      "quantity": 2,
      "price": 100.00
    }
  ],
  "idempotencyKey": "test-key-1"
}
```

**Via cURL:**
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "items": [{"productId": "prod-1", "quantity": 2, "price": 100.00}],
    "idempotencyKey": "test-key-1"
  }'
```

### Passo 3: Ver Métricas no Grafana

Após criar alguns pedidos, volte ao Grafana e veja:
- ✅ Mensagens sendo consumidas em tempo real
- ✅ Distribuição por tópico Kafka
- ✅ Latência de processamento
- ✅ Taxa de sucesso/erro

## 🔍 Verificar se Está Funcionando

### 1. Verificar Aplicação
```bash
curl http://localhost:8081/actuator/health
```
Deve retornar: `{"status":"UP"}`

### 2. Verificar Métricas
```bash
curl http://localhost:8081/actuator/prometheus | grep kafka
```
Deve mostrar métricas começando com `kafka_consumer_`

### 3. Verificar Prometheus
1. Acesse http://localhost:9090
2. Vá em **Status** → **Targets**
3. Deve mostrar `smart-order-orchestrator` como **UP**

### 4. Verificar Grafana
1. Acesse http://localhost:3000
2. Vá em **Configuration** → **Data Sources**
3. Deve ter **Prometheus** configurado e funcionando

## 🐛 Troubleshooting

### Aplicação não inicia
- Verifique se PostgreSQL está rodando: `docker ps`
- Verifique logs: `docker logs smartorder-postgres`

### Prometheus não coleta métricas
- Verifique se a aplicação está em: http://localhost:8081/actuator/prometheus
- No Windows, o `host.docker.internal` deve funcionar automaticamente
- Verifique logs: `docker logs smartorder-prometheus`

### Grafana não mostra dados
- Verifique se o Prometheus está configurado como data source
- Verifique se há métricas no Prometheus: http://localhost:9090/graph
- Procure por métricas: `kafka_consumer_messages_total_total`

### Kafka não está rodando
- O Kafka precisa estar rodando separadamente (não está no docker-compose)
- Configure: `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- Ou use um Kafka local/cloud

## 📚 Próximos Passos

1. **Explorar o Dashboard**: Veja todas as métricas disponíveis no Grafana
2. **Criar Mais Pedidos**: Gere tráfego para ver métricas em tempo real
3. **Ler Documentação**: Veja [monitoring/README.md](monitoring/README.md) para detalhes técnicos
4. **Explorar Código**: Veja como as métricas são coletadas em `KafkaConsumerMetrics`

## 🎯 O que Você Vai Ver

Com o sistema funcionando, você terá:

✅ **Dashboard Visual** mostrando consumo Kafka em tempo real  
✅ **Métricas de Performance** (latência, throughput)  
✅ **Distribuição de Carga** por tópico e partição  
✅ **Monitoramento de Erros** e taxa de sucesso  
✅ **Observabilidade Completa** para demonstrar em entrevistas  

---

**💡 Dica**: Este sistema demonstra capacidade de gerenciar sistemas distribuídos com observabilidade, uma skill muito valorizada por recrutadores!

