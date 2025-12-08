# Segurança - Gerenciamento de Secrets

## 🔒 Visão Geral

Este documento descreve a estratégia de segurança para gerenciamento de chaves de API, senhas e outras informações sensíveis no projeto **Smart Order Orchestrator**, seguindo padrões enterprise e boas práticas de segurança da informação.

## ⚠️ Por que NÃO usar Variáveis de Ambiente Diretamente em Produção?

### Problemas de Segurança

1. **Exposição em Logs**: Variáveis de ambiente podem aparecer em logs de aplicação, containers, ou sistemas de CI/CD
2. **Versionamento Acidental**: Risco de commitar secrets no Git (mesmo com `.env` no `.gitignore`)
3. **Acesso Amplo**: Qualquer pessoa com acesso ao container/pod pode ver todas as variáveis
4. **Rotação Difícil**: Trocar uma chave requer redeploy completo
5. **Auditoria Limitada**: Difícil rastrear quem acessou qual secret e quando

### Solução: Google Cloud Secret Manager

**Benefícios:**
- ✅ **Criptografia em Repouso**: Secrets são criptografados automaticamente
- ✅ **Criptografia em Trânsito**: Comunicação TLS/SSL
- ✅ **Auditoria Completa**: Logs de quem acessou cada secret
- ✅ **Rotação Fácil**: Atualizar secret sem redeploy
- ✅ **Controle de Acesso**: IAM roles específicas (princípio do menor privilégio)
- ✅ **Versionamento**: Histórico de versões de cada secret
- ✅ **Integração Nativa**: Cloud Run/App Engine acessam automaticamente

---

## 🏗️ Arquitetura de Segurança

```
┌─────────────────────────────────────────────────────────────┐
│                  Google Cloud Platform                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────┐                                         │
│  │  Cloud Run      │                                         │
│  │  (Backend)      │                                         │
│  │                 │                                         │
│  │  Service Account│──────────┐                             │
│  │  (IAM Role)     │          │                             │
│  └─────────────────┘          │                             │
│                               │                             │
│                               ▼                             │
│                    ┌──────────────────┐                     │
│                    │ Secret Manager   │                     │
│                    │                  │                     │
│                    │ • database-     │                     │
│                    │   password       │                     │
│                    │ • abacatepay-    │                     │
│                    │   api-key        │                     │
│                    │ • openai-api-key │                     │
│                    │                  │                     │
│                    │ [Criptografado]  │                     │
│                    └──────────────────┘                     │
│                                                              │
│  ┌─────────────────┐                                         │
│  │  Spring Boot   │                                         │
│  │  Application    │                                         │
│  │                 │                                         │
│  │  Lê secrets via│                                         │
│  │  Secret Manager │                                         │
│  │  Client        │                                         │
│  └─────────────────┘                                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Secrets Necessários

### 1. **database-password**
- **Descrição**: Senha do banco de dados PostgreSQL (Cloud SQL)
- **Tipo**: String
- **Rotação**: A cada 90 dias (recomendado)
- **Acesso**: Apenas Service Account do Cloud Run

### 2. **abacatepay-api-key**
- **Descrição**: Chave de API do gateway de pagamento AbacatePay
- **Tipo**: String
- **Rotação**: Conforme política do provedor
- **Acesso**: Apenas Service Account do Cloud Run

### 3. **openai-api-key**
- **Descrição**: Chave de API do OpenAI para análise de risco
- **Tipo**: String
- **Rotação**: Conforme política do OpenAI
- **Acesso**: Apenas Service Account do Cloud Run

---

## 🔧 Implementação

### 1. Adicionar Dependência (pom.xml)

```xml
<!-- Google Cloud Secret Manager -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-secretmanager</artifactId>
    <version>2.4.0</version>
</dependency>

<!-- Spring Cloud GCP (opcional, mas recomendado) -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-secretmanager</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 2. Configuração Spring Boot

O Spring Cloud GCP permite acessar secrets diretamente via variáveis de ambiente com o prefixo `sm://`:

```yaml
# application-prod.yml
spring:
  cloud:
    gcp:
      secretmanager:
        enabled: true
        project-id: ${GCP_PROJECT_ID}

# Usar secrets do Secret Manager
abacatepay:
  api:
    key: ${sm://abacatepay-api-key}

openai:
  api:
    key: ${sm://openai-api-key}

spring:
  datasource:
    password: ${sm://database-password}
```

### 3. Alternativa: Acesso Programático (Mais Controle)

Se precisar de mais controle, use o cliente diretamente:

```java
@Configuration
public class SecretManagerConfig {
    
    @Bean
    public SecretManagerServiceClient secretManagerClient() {
        return SecretManagerServiceClient.create();
    }
}
```

---

## 🚀 Setup no GCP

### Passo 1: Habilitar Secret Manager API

```bash
gcloud services enable secretmanager.googleapis.com
```

### Passo 2: Criar Secrets

```bash
# Definir variáveis
export PROJECT_ID="seu-projeto-gcp"
export REGION="us-central1"

# Criar secret para senha do banco
echo -n "sua_senha_segura_aqui" | gcloud secrets create database-password \
  --project=$PROJECT_ID \
  --data-file=-

# Criar secret para AbacatePay
echo -n "sua_chave_abacatepay" | gcloud secrets create abacatepay-api-key \
  --project=$PROJECT_ID \
  --data-file=-

# Criar secret para OpenAI
echo -n "sua_chave_openai" | gcloud secrets create openai-api-key \
  --project=$PROJECT_ID \
  --data-file=-
```

### Passo 3: Configurar Service Account

```bash
# Obter número do projeto
export PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

# Service Account padrão do Cloud Run
export SERVICE_ACCOUNT="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

# Dar permissão de leitura nos secrets
gcloud secrets add-iam-policy-binding database-password \
  --project=$PROJECT_ID \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding abacatepay-api-key \
  --project=$PROJECT_ID \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding openai-api-key \
  --project=$PROJECT_ID \
  --member="serviceAccount:${SERVICE_ACCOUNT}" \
  --role="roles/secretmanager.secretAccessor"
```

### Passo 4: Deploy no Cloud Run com Secrets

```bash
gcloud run deploy smart-order-backend \
  --image gcr.io/${PROJECT_ID}/smart-order-backend \
  --platform managed \
  --region ${REGION} \
  --set-secrets="DATABASE_PASSWORD=database-password:latest,ABACATEPAY_API_KEY=abacatepay-api-key:latest,OPENAI_API_KEY=openai-api-key:latest" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,GCP_PROJECT_ID=${PROJECT_ID}" \
  --service-account ${SERVICE_ACCOUNT}
```

---

## 🔐 Boas Práticas de Segurança

### 1. **Princípio do Menor Privilégio**
- Service Account do Cloud Run deve ter **apenas** `roles/secretmanager.secretAccessor`
- **NUNCA** dar `roles/secretmanager.admin` ou `roles/secretmanager.secretAccessor` em nível de projeto

### 2. **Versionamento de Secrets**
- Use `:latest` apenas em desenvolvimento
- Em produção, use versões específicas: `database-password:1`
- Permite rollback rápido se houver problema

### 3. **Rotação Regular**
- Rotacione secrets a cada 90 dias (ou conforme política)
- Use versões para transição suave:
  ```bash
  # Criar nova versão
  echo -n "nova_senha" | gcloud secrets versions add database-password --data-file=-
  
  # Atualizar referência no Cloud Run (sem downtime)
  gcloud run services update smart-order-backend \
    --set-secrets="DATABASE_PASSWORD=database-password:2"
  ```

### 4. **Auditoria e Monitoramento**
- Habilite Cloud Audit Logs para Secret Manager
- Monitore acessos suspeitos
- Configure alertas para falhas de acesso

### 5. **Não Commitar Secrets**
- ✅ Use `.gitignore` para `.env` e arquivos de secrets
- ✅ Use `git-secrets` ou `truffleHog` para detectar secrets no código
- ✅ Use pre-commit hooks para validar

### 6. **Desenvolvimento Local**

Para desenvolvimento local, use variáveis de ambiente (arquivo `.env` não versionado):

```bash
# .env (não commitar!)
ABACATEPAY_API_KEY=sua_chave_dev
OPENAI_API_KEY=sua_chave_dev
DATABASE_PASSWORD=senha_local
```

Adicione ao `.gitignore`:
```
.env
.env.local
.env.*.local
```

---

## 🧪 Testes Locais com Secret Manager

### Opção 1: Usar Service Account Local

```bash
# Autenticar com Service Account
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"

# Executar aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Opção 2: Usar Application Default Credentials

```bash
# Autenticar via gcloud
gcloud auth application-default login

# Executar aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 📊 Custos

| Operação | Custo |
|----------|-------|
| Armazenar secret | $0.06 por secret/mês |
| Acessar secret (leitura) | $0.03 por 10.000 operações |
| Versões de secret | Grátis (até 10 versões) |

**Estimativa para este projeto:**
- 3 secrets × $0.06 = **$0.18/mês**
- ~100.000 acessos/mês = **$0.30/mês**
- **Total: ~$0.50/mês**

---

## 🚨 Troubleshooting

### Erro: "Permission denied"

**Causa**: Service Account não tem permissão para acessar secret.

**Solução**:
```bash
gcloud secrets add-iam-policy-binding SECRET_NAME \
  --member="serviceAccount:SERVICE_ACCOUNT_EMAIL" \
  --role="roles/secretmanager.secretAccessor"
```

### Erro: "Secret not found"

**Causa**: Nome do secret incorreto ou não existe.

**Solução**: Verificar nome exato:
```bash
gcloud secrets list
```

### Erro: "Authentication failed"

**Causa**: Service Account não autenticado ou credenciais inválidas.

**Solução**: Verificar credenciais:
```bash
gcloud auth application-default print-access-token
```

---

## 📚 Referências

- [Google Cloud Secret Manager Documentation](https://cloud.google.com/secret-manager/docs)
- [Spring Cloud GCP Secret Manager](https://spring.io/projects/spring-cloud-gcp)
- [OWASP Secrets Management](https://owasp.org/www-community/vulnerabilities/Use_of_hard-coded_cryptographic_key)
- [12-Factor App - Config](https://12factor.net/config)

---

## ✅ Checklist de Segurança

- [ ] Secrets criados no Secret Manager
- [ ] Service Account configurado com permissões mínimas
- [ ] Secrets configurados no Cloud Run/App Engine
- [ ] `.env` adicionado ao `.gitignore`
- [ ] Pre-commit hooks configurados (opcional)
- [ ] Cloud Audit Logs habilitado
- [ ] Rotação de secrets agendada
- [ ] Documentação atualizada

---

**Última Atualização:** 2024

