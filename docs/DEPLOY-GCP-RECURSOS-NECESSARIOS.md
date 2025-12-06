# Deploy no GCP - Recursos Necessários e Preparação

## 📋 Status Atual do Projeto

### ✅ O que já está pronto para deploy

#### Backend (Spring Boot)
- ✅ **Aplicação Spring Boot 3.2+** configurada
- ✅ **Perfil de produção** (`application-prod.yml`) configurado
- ✅ **Variáveis de ambiente** suportadas (12-Factor App)
- ✅ **Flyway migrations** prontas para executar no banco
- ✅ **Health checks** (Actuator) configurados
- ✅ **Métricas Prometheus** habilitadas
- ✅ **Virtual Threads** habilitadas (Java 21)
- ✅ **Resilience4j** configurado (Circuit Breaker, Retry)

#### Frontend (React + Vite)
- ✅ **Build estático** (Vite gera `dist/` com assets otimizados)
- ✅ **TypeScript** configurado
- ✅ **Proxy configurado** para API (desenvolvimento)

### ⚠️ O que falta para deploy no GCP

#### Backend
- ❌ **Dockerfile** para containerização
- ❌ **Cloud Build** configuration (cloudbuild.yaml)
- ❌ **App Engine** ou **Cloud Run** configuration
- ❌ **Secret Manager** para chaves de API
- ❌ **Service Account** com permissões adequadas

#### Frontend
- ❌ **Dockerfile** para containerização (opcional, pode usar Cloud Storage)
- ❌ **Variáveis de ambiente** para URL da API em produção
- ❌ **Build script** otimizado para produção
- ❌ **Cloud Storage + CDN** ou **Cloud Run** configuration

---

## 🏗️ Arquitetura de Deploy no GCP

### Opção 1: Cloud Run (Recomendado - Serverless)

```
┌─────────────────────────────────────────────────────────┐
│                    Google Cloud Platform                 │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐         ┌──────────────┐              │
│  │  Cloud Run  │         │  Cloud Run   │              │
│  │  (Frontend) │────────▶│  (Backend)   │              │
│  │             │         │              │              │
│  └──────────────┘         └──────┬───────┘              │
│                                  │                       │
│                          ┌──────▼───────┐              │
│                          │  Cloud SQL   │              │
│                          │ (PostgreSQL) │              │
│                          └──────────────┘              │
│                                                          │
│  ┌──────────────┐         ┌──────────────┐              │
│  │ Secret       │         │ Cloud Build  │              │
│  │ Manager      │         │ (CI/CD)      │              │
│  └──────────────┘         └──────────────┘              │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**Vantagens:**
- ✅ Serverless (paga apenas pelo uso)
- ✅ Auto-scaling automático
- ✅ HTTPS nativo
- ✅ Integração fácil com outros serviços GCP
- ✅ Suporta containers Docker

**Custos Estimados:**
- Cloud Run: ~$0.40 por milhão de requisições + CPU/Memória usada
- Cloud SQL: ~$25-50/mês (db-f1-micro ou db-g1-small)
- Secret Manager: ~$0.06 por secret/mês
- **Total estimado: ~$30-60/mês** (baixo tráfego)

---

### Opção 2: App Engine (Gerenciado)

```
┌─────────────────────────────────────────────────────────┐
│                    Google Cloud Platform                 │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐         ┌──────────────┐              │
│  │ App Engine  │         │  App Engine  │              │
│  │ (Frontend)  │────────▶│  (Backend)   │              │
│  │             │         │              │              │
│  └──────────────┘         └──────┬───────┘              │
│                                  │                       │
│                          ┌──────▼───────┐              │
│                          │  Cloud SQL   │              │
│                          │ (PostgreSQL) │              │
│                          └──────────────┘              │
└─────────────────────────────────────────────────────────┘
```

**Vantagens:**
- ✅ Totalmente gerenciado (sem gerenciar servidores)
- ✅ Auto-scaling automático
- ✅ HTTPS nativo
- ✅ Integração com outros serviços GCP

**Desvantagens:**
- ⚠️ Menos flexível que Cloud Run
- ⚠️ Requer configuração específica do App Engine

**Custos Estimados:**
- App Engine: ~$0.05 por instância-hora (F1) + tráfego
- Cloud SQL: ~$25-50/mês
- **Total estimado: ~$30-60/mês** (baixo tráfego)

---

### Opção 3: GKE (Kubernetes) - Para Alta Escala

**Quando usar:**
- Alta escala (milhões de requisições)
- Necessidade de controle fino sobre infraestrutura
- Múltiplos ambientes (dev, staging, prod)

**Custos Estimados:**
- GKE: ~$73/mês (cluster mínimo) + nodes
- Cloud SQL: ~$25-50/mês
- **Total estimado: ~$100+/mês**

---

## 📦 Recursos GCP Necessários

### 1. **Cloud SQL (PostgreSQL)** - OBRIGATÓRIO

**O que é:** Banco de dados PostgreSQL gerenciado.

**Configuração Recomendada:**
- **Tier:** `db-f1-micro` (desenvolvimento) ou `db-g1-small` (produção)
- **Região:** Mesma região do Cloud Run/App Engine
- **Versão:** PostgreSQL 15 ou 16
- **Backup:** Habilitado (diário)
- **High Availability:** Opcional (aumenta custo)

**Variáveis de Ambiente Necessárias:**
```bash
DATABASE_URL=jdbc:postgresql:///smartorder?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=<senha_segura>
```

**Custo Estimado:**
- `db-f1-micro`: ~$7-10/mês
- `db-g1-small`: ~$25-30/mês
- Com HA: +100% do custo

**Como Criar:**
```bash
gcloud sql instances create smartorder-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=<senha_segura>
```

---

### 2. **Cloud Run** (Backend) - RECOMENDADO

**O que é:** Plataforma serverless para executar containers.

**Configuração Recomendada:**
- **CPU:** 1 vCPU
- **Memória:** 512MB - 1GB (Virtual Threads são leves)
- **Concorrência:** 80 requisições por instância (padrão)
- **Min Instances:** 0 (scale to zero)
- **Max Instances:** 10 (ajustar conforme necessidade)

**Variáveis de Ambiente:**
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<connection_string_cloud_sql>
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=<from_secret_manager>
ABACATEPAY_API_KEY=<from_secret_manager>
OPENAI_API_KEY=<from_secret_manager>
```

**Custo Estimado:**
- CPU: $0.00002400 por vCPU-segundo
- Memória: $0.00000250 por GB-segundo
- Requisições: $0.40 por milhão
- **Exemplo:** 100k requisições/mês = ~$0.04 + CPU/Memória (~$5-10/mês)

**Como Deployar:**
```bash
# Build e push da imagem
gcloud builds submit --tag gcr.io/PROJECT_ID/smart-order-backend

# Deploy no Cloud Run
gcloud run deploy smart-order-backend \
  --image gcr.io/PROJECT_ID/smart-order-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod \
  --add-cloudsql-instances PROJECT_ID:REGION:INSTANCE_NAME \
  --memory 1Gi \
  --cpu 1
```

---

### 3. **Cloud Run** (Frontend) - RECOMENDADO

**O que é:** Serve o build estático do React via container Nginx.

**Configuração Recomendada:**
- **CPU:** 0.5 vCPU
- **Memória:** 256MB
- **Concorrência:** 100 requisições por instância
- **Min Instances:** 0
- **Max Instances:** 5

**Variáveis de Ambiente:**
```bash
VITE_API_BASE_URL=https://smart-order-backend-xxxxx.run.app
```

**Custo Estimado:**
- Similar ao backend, mas menor (menos CPU/Memória)
- **~$2-5/mês** (baixo tráfego)

**Alternativa: Cloud Storage + Cloud CDN**
- Mais barato para frontend estático
- **~$0.50-2/mês** (armazenamento + tráfego)

---

### 4. **Secret Manager** - OBRIGATÓRIO

**O que é:** Armazena chaves de API e senhas de forma segura.

**Secrets Necessários:**
- `database-password` - Senha do Cloud SQL
- `abacatepay-api-key` - Chave da API AbacatePay
- `openai-api-key` - Chave da API OpenAI

**Custo Estimado:**
- $0.06 por secret/mês
- $0.03 por 10.000 operações de acesso
- **Total: ~$0.20/mês** (3 secrets)

**Como Criar:**
```bash
# Criar secret
echo -n "sua_senha_aqui" | gcloud secrets create database-password --data-file=-

# Dar permissão ao Cloud Run
gcloud secrets add-iam-policy-binding database-password \
  --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

---

### 5. **Cloud Build** (CI/CD) - RECOMENDADO

**O que é:** Pipeline de build e deploy automatizado.

**Configuração:**
- Build automático no push para `main`
- Deploy automático no Cloud Run
- Execução de testes antes do deploy

**Custo Estimado:**
- Primeiros 120 minutos/dia: **GRÁTIS**
- Depois: $0.003 por minuto
- **Total: ~$0-5/mês** (depende do uso)

---

### 6. **Cloud Storage** (Frontend Alternativo) - OPCIONAL

**O que é:** Armazenamento de arquivos estáticos.

**Quando usar:** Se preferir servir frontend via Cloud Storage + CDN ao invés de Cloud Run.

**Configuração:**
- Bucket público para frontend
- Cloud CDN habilitado
- Custom domain (opcional)

**Custo Estimado:**
- Armazenamento: $0.020 por GB/mês
- Tráfego: $0.12 por GB (primeiros 10TB)
- **Total: ~$0.50-2/mês** (baixo tráfego)

---

## 🔧 O que Precisa ser Criado

### 1. **Dockerfile para Backend**

**Localização:** `backend/Dockerfile`

**Conteúdo necessário:**
- Base image: `eclipse-temurin:21-jre` (Java 21)
- Copiar JAR do build
- Expor porta 8080
- Health check
- Variáveis de ambiente

---

### 2. **Dockerfile para Frontend** (se usar Cloud Run)

**Localização:** `frontend/Dockerfile`

**Conteúdo necessário:**
- Build do Vite (`npm run build`)
- Servir com Nginx
- Configurar proxy para API

**Alternativa:** Usar Cloud Storage (mais simples e barato)

---

### 3. **cloudbuild.yaml** (CI/CD)

**Localização:** `.cloudbuild.yaml` (raiz do projeto)

**Conteúdo necessário:**
- Build do backend (Maven)
- Build do frontend (npm)
- Push de imagens para Container Registry
- Deploy no Cloud Run

---

### 4. **app.yaml** (se usar App Engine)

**Localização:** `backend/app.yaml`

**Conteúdo necessário:**
- Runtime: Java 21
- Configuração de instâncias
- Variáveis de ambiente
- Conexão com Cloud SQL

---

### 5. **Configuração de Variáveis de Ambiente**

**Backend:**
- `SPRING_PROFILES_ACTIVE=prod`
- `DATABASE_URL` (Cloud SQL connection string)
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD` (do Secret Manager)
- `ABACATEPAY_API_KEY` (do Secret Manager)
- `OPENAI_API_KEY` (do Secret Manager)

**Frontend:**
- `VITE_API_BASE_URL` (URL do backend no Cloud Run)

---

## 📊 Resumo de Custos Estimados (Mensal)

### Opção 1: Cloud Run (Recomendado)

| Recurso | Configuração | Custo Estimado |
|---------|--------------|----------------|
| **Cloud SQL** | db-f1-micro | $7-10 |
| **Cloud Run (Backend)** | 1 vCPU, 1GB RAM | $5-10 |
| **Cloud Run (Frontend)** | 0.5 vCPU, 256MB | $2-5 |
| **Secret Manager** | 3 secrets | $0.20 |
| **Cloud Build** | 120 min/dia grátis | $0-5 |
| **Tráfego** | Baixo volume | $0-2 |
| **TOTAL** | | **~$15-30/mês** |

### Opção 2: Cloud Run + Cloud Storage (Frontend)

| Recurso | Configuração | Custo Estimado |
|---------|--------------|----------------|
| **Cloud SQL** | db-f1-micro | $7-10 |
| **Cloud Run (Backend)** | 1 vCPU, 1GB RAM | $5-10 |
| **Cloud Storage + CDN** | Frontend estático | $0.50-2 |
| **Secret Manager** | 3 secrets | $0.20 |
| **Cloud Build** | 120 min/dia grátis | $0-5 |
| **TOTAL** | | **~$13-28/mês** |

### Opção 3: App Engine

| Recurso | Configuração | Custo Estimado |
|---------|--------------|----------------|
| **Cloud SQL** | db-f1-micro | $7-10 |
| **App Engine (Backend)** | F1 instance | $5-10 |
| **App Engine (Frontend)** | F1 instance | $2-5 |
| **Secret Manager** | 3 secrets | $0.20 |
| **TOTAL** | | **~$15-30/mês** |

**Nota:** Custos podem variar significativamente com tráfego. Use a [Calculadora de Preços do GCP](https://cloud.google.com/products/calculator) para estimativas precisas.

---

## ✅ Checklist de Preparação para Deploy

### Backend

- [ ] Criar Dockerfile
- [ ] Testar build local do container
- [ ] Configurar Cloud SQL (PostgreSQL)
- [ ] Criar secrets no Secret Manager
- [ ] Configurar Service Account com permissões
- [ ] Testar conexão com Cloud SQL
- [ ] Configurar Cloud Run ou App Engine
- [ ] Configurar variáveis de ambiente
- [ ] Testar health check
- [ ] Configurar domínio customizado (opcional)

### Frontend

- [ ] Configurar variável de ambiente para API URL
- [ ] Testar build de produção (`npm run build`)
- [ ] Criar Dockerfile (se usar Cloud Run) OU configurar Cloud Storage
- [ ] Configurar Cloud Run ou Cloud Storage + CDN
- [ ] Testar integração com backend
- [ ] Configurar domínio customizado (opcional)

### CI/CD

- [ ] Criar cloudbuild.yaml
- [ ] Configurar triggers no Cloud Build
- [ ] Testar pipeline completo
- [ ] Configurar notificações (opcional)

### Segurança

- [ ] Configurar IAM roles adequadas
- [ ] Habilitar HTTPS (automático no Cloud Run/App Engine)
- [ ] Configurar CORS no backend (se necessário)
- [ ] Revisar permissões de Service Accounts

---

## 🚀 Próximos Passos

1. **Criar Dockerfile para Backend**
2. **Criar Dockerfile para Frontend** (ou configurar Cloud Storage)
3. **Configurar Cloud SQL**
4. **Criar secrets no Secret Manager**
5. **Configurar Cloud Run** (backend e frontend)
6. **Criar cloudbuild.yaml** para CI/CD
7. **Testar deploy completo**

---

## 📚 Documentação de Referência

- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Cloud SQL Documentation](https://cloud.google.com/sql/docs)
- [Secret Manager Documentation](https://cloud.google.com/secret-manager/docs)
- [Cloud Build Documentation](https://cloud.google.com/build/docs)
- [App Engine Documentation](https://cloud.google.com/appengine/docs)
- [GCP Pricing Calculator](https://cloud.google.com/products/calculator)

---

**Última Atualização:** 2024

