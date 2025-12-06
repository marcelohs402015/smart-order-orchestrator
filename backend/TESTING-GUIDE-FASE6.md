# Guia de Testes - Fase 6: Integração com OpenAI

## 🎯 Relevância da Fase 6 para Apresentação

### Por que esta fase é importante?

A integração com OpenAI demonstra **múltiplas competências técnicas** essenciais para um desenvolvedor sênior:

#### 1. **Integração com APIs Externas Modernas**
- Demonstra conhecimento de APIs REST modernas
- Uso de WebClient (reativo) ao invés de RestTemplate (legado)
- Tratamento de autenticação Bearer token
- Parsing de respostas JSON complexas

#### 2. **Arquitetura Hexagonal na Prática**
- **Port**: `RiskAnalysisPort` (contrato no domínio)
- **Adapter**: `OpenAIRiskAnalysisAdapter` (implementação isolada)
- **Inversão de Dependência**: Domínio não conhece OpenAI
- **Testabilidade**: Fácil mockar para testes

#### 3. **Resiliência e Padrões de Integração**
- **Circuit Breaker**: Protege contra falhas em cascata
- **Retry**: Tenta novamente em falhas transitórias
- **Fallback**: Degradação graciosa (retorna PENDING)
- **Timeout**: Evita requisições travadas

#### 4. **IA Generativa em Produção**
- Demonstra conhecimento de tecnologias modernas
- Uso de prompts estruturados
- Análise semântica vs regras hardcoded
- Escalabilidade para grandes volumes

#### 5. **Separação de Responsabilidades**
- Use Case orquestra a lógica de negócio
- Adapter isola detalhes da API externa
- Domínio permanece puro (sem dependências)

---

## 🧪 O que Testar Antes da Fase 7

### Testes Críticos para Demonstrar

#### 1. **Teste do Use Case (AnalyzeRiskUseCase)**

**Por que é importante?**
- Demonstra que a orquestração funciona corretamente
- Valida integração entre camadas (Application → Domain → Infrastructure)
- Testa fallback gracioso quando IA falha

**Cenários a testar:**
- ✅ Análise bem-sucedida (LOW)
- ✅ Análise bem-sucedida (HIGH)
- ✅ Pedido não encontrado (erro)
- ✅ Pedido não está PAID (validação de estado)
- ✅ OpenAI falha → fallback para PENDING
- ✅ Persistência do riskLevel atualizado

#### 2. **Teste do Adapter (OpenAIRiskAnalysisAdapter)**

**Por que é importante?**
- Valida construção do prompt
- Testa parsing de respostas
- Demonstra tratamento de erros

**Cenários a testar:**
- ✅ Resposta "LOW" → parse correto
- ✅ Resposta "HIGH" → parse correto
- ✅ Resposta inválida → fallback PENDING
- ✅ Erro de API (401, 500) → fallback PENDING
- ✅ Timeout → fallback PENDING
- ✅ Circuit Breaker aberto → fallback

#### 3. **Teste de Integração End-to-End**

**Por que é importante?**
- Demonstra fluxo completo funcionando
- Valida persistência no banco
- Testa transações

**Cenários a testar:**
- ✅ Criar pedido → Pagar → Analisar risco → Verificar riskLevel no banco

---

## 📋 Checklist de Testes

### ✅ Testes Unitários (Já Criados)
- [x] `OpenAIRiskAnalysisAdapterTest` - Testa adapter isolado

### ⚠️ Testes Faltantes (Importantes)

1. **`AnalyzeRiskUseCaseTest`** - **CRÍTICO**
   - Testa orquestração completa
   - Valida integração entre camadas
   - Demonstra fallback gracioso

2. **Teste de Integração** - **IMPORTANTE**
   - Testa fluxo completo com banco real (H2)
   - Valida persistência

---

## 🎤 Como Apresentar na Entrevista

### Script de Apresentação

**1. Contexto do Problema:**
> "Após um pedido ser pago, precisamos classificar o risco para decidir se processamos automaticamente ou requer revisão manual. Em vez de regras fixas, usei IA generativa para análise contextual."

**2. Arquitetura:**
> "Implementei usando Arquitetura Hexagonal. O domínio define o contrato (`RiskAnalysisPort`), e a Infrastructure implementa com OpenAI. Isso permite trocar o provedor de IA sem alterar o domínio."

**3. Resiliência:**
> "Usei Resilience4j com Circuit Breaker e Retry. Se a OpenAI estiver offline, o sistema continua funcionando, mantendo o risco como PENDING. Isso demonstra resiliência em integrações externas."

**4. Testabilidade:**
> "Como o domínio não conhece OpenAI, posso testar o Use Case mockando apenas a interface. Isso garante testes rápidos e isolados."

**5. Prompt Engineering:**
> "Estruturei o prompt para que a IA retorne apenas 'LOW' ou 'HIGH', facilitando o parsing e garantindo consistência. Usei temperatura 0.0 para respostas determinísticas."

---

## 🔍 Pontos de Destaque Técnico

### 1. **WebClient vs RestTemplate**
- WebClient é reativo e não bloqueia threads
- Compatível com Virtual Threads (Java 21)
- Melhor performance em alta concorrência

### 2. **Circuit Breaker Pattern**
- Protege contra falhas em cascata
- Abre circuito após muitas falhas
- Fallback gracioso quando aberto

### 3. **Separação de Concerns**
- Use Case: orquestração
- Adapter: integração técnica
- Domínio: regras de negócio

### 4. **Error Handling**
- Não lança exceções, retorna resultado
- Permite degradação graciosa
- Sistema continua funcionando

---

## 📊 Métricas para Demonstrar

1. **Cobertura de Testes**: Mostrar que tem testes unitários e de integração
2. **Resiliência**: Demonstrar que sistema funciona mesmo com IA offline
3. **Performance**: WebClient não bloqueia threads
4. **Manutenibilidade**: Fácil trocar OpenAI por outro provedor

---

## 🚀 Próximos Passos

Após testar a Fase 6, você estará pronto para:
- **Fase 7**: Saga Pattern (orquestração de transações distribuídas)
- **Fase 8**: REST API (expor funcionalidades via HTTP)
- **Fase 9**: Virtual Threads (otimização de performance)

