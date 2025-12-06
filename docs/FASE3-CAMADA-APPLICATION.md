# Fase 3: Camada Application (Use Cases)

## 🎯 Objetivo

Implementar casos de uso e orquestração da aplicação, coordenando operações de negócio sem conter regras de negócio (que ficam no domínio).

## ✅ Entregas

### 1. Use Cases Implementados

#### CreateOrderUseCase

**Responsabilidades:**
- Orquestrar criação de pedido
- Validar dados de entrada
- Calcular total do pedido
- Persistir pedido
- Notificar sobre criação

**Fluxo:**
1. Valida command
2. Cria entidade Order com status PENDING
3. Calcula total (regra de negócio no domínio)
4. Persiste através do OrderRepositoryPort
5. Notifica através do NotificationPort

**Por que Use Case separado?**
- **Single Responsibility**: Uma classe, uma responsabilidade
- **Testabilidade**: Fácil testar isoladamente (mock das portas)
- **Reutilização**: Pode ser chamado por diferentes adaptadores (REST, CLI, etc.)
- **Orquestração**: Coordena múltiplas operações sem acoplar ao domínio

#### ProcessPaymentUseCase

**Responsabilidades:**
- Buscar pedido pelo ID
- Validar que pedido está em estado válido para pagamento
- Chamar gateway de pagamento (com Circuit Breaker via Resilience4j)
- Atualizar status do pedido baseado no resultado
- Notificar sobre resultado do pagamento

**Fluxo:**
1. Valida que pedido existe e está PENDING
2. Cria PaymentRequest com dados do pedido
3. Chama PaymentGatewayPort (protegido por Circuit Breaker)
4. Se sucesso: marca pedido como PAID
5. Se falha: marca pedido como PAYMENT_FAILED
6. Persiste mudança e notifica

**Resiliência:**
- Circuit Breaker protege chamada ao gateway
- Se gateway estiver indisponível, Circuit Breaker abre e retorna falha rapidamente

#### AnalyzeRiskUseCase

**Responsabilidades:**
- Buscar pedido (deve estar PAID)
- Validar que pedido está em estado válido para análise
- Chamar serviço de IA através do RiskAnalysisPort
- Atualizar nível de risco do pedido
- Tratar falhas graciosamente (fallback para PENDING)

**Fluxo:**
1. Valida que pedido existe e está PAID
2. Cria RiskAnalysisRequest com metadados do pedido
3. Chama RiskAnalysisPort (pode falhar - fallback gracioso)
4. Atualiza riskLevel do pedido (LOW, HIGH, ou mantém PENDING se falhou)
5. Persiste mudança

**Resiliência:**
- Se serviço de IA estiver indisponível, pedido mantém risco PENDING
- Permite que sistema continue funcionando mesmo com IA offline

#### UpdateOrderStatusUseCase

**Responsabilidades:**
- Atualizar status de um pedido
- Validar transição de estado
- Persistir mudança

### 2. Commands (CQRS)

#### CreateOrderCommand

**Dados:**
- `customerId`: ID do cliente
- `customerName`: Nome do cliente
- `customerEmail`: Email do cliente
- `items`: Lista de itens do pedido

**Por que Command separado?**
- **Separação de Concerns**: Dados de entrada vs. modelo de domínio
- **Validação**: Validações de entrada diferentes de regras de negócio
- **Flexibilidade**: Command pode ter campos diferentes da entidade
- **Segurança**: Não expõe entidade de domínio diretamente

#### ProcessPaymentCommand

**Dados:**
- `orderId`: ID do pedido
- `currency`: Moeda do pagamento
- `paymentMethod`: Método de pagamento

#### AnalyzeRiskCommand

**Dados:**
- `orderId`: ID do pedido
- `paymentMethod`: Método de pagamento

#### UpdateOrderStatusCommand

**Dados:**
- `orderId`: ID do pedido
- `newStatus`: Novo status

### 3. Saga Pattern (Orquestração)

#### OrderSagaOrchestrator

**Responsabilidades:**
- Orquestrar os 3 passos da saga (Criar Pedido → Processar Pagamento → Analisar Risco)
- Rastrear estado de cada passo para observabilidade
- Executar compensação automática em caso de falha
- Persistir histórico completo de execução

**Fluxo:**
1. Step 1: Criar pedido (status: PENDING)
2. Step 2: Processar pagamento (status: PAID ou PAYMENT_FAILED)
3. Step 3: Analisar risco (apenas se pagamento sucedeu)
4. Compensação: Se pagamento falhar, cancelar pedido

**Por que Saga Pattern?**
- **Consistência Eventual**: Garante que todas as operações sejam executadas na ordem correta
- **Compensação**: Rollback automático se algum passo falhar
- **Observabilidade**: Rastreamento completo de cada execução
- **Padrão Enterprise**: Usado em microserviços e sistemas distribuídos

## 🏗️ Arquitetura

### Camada Application

```
application/
├── usecase/
│   ├── CreateOrderUseCase.java
│   ├── ProcessPaymentUseCase.java
│   ├── AnalyzeRiskUseCase.java
│   ├── UpdateOrderStatusUseCase.java
│   ├── CreateOrderCommand.java
│   ├── ProcessPaymentCommand.java
│   ├── AnalyzeRiskCommand.java
│   └── UpdateOrderStatusCommand.java
├── saga/
│   ├── OrderSagaOrchestrator.java
│   ├── OrderSagaCommand.java
│   └── OrderSagaResult.java
└── exception/
    ├── DomainException.java
    ├── OrderNotFoundException.java
    └── InvalidOrderStatusException.java
```

### Dependências

- **Depende de**: Domain (portas e entidades)
- **Não conhece**: Infrastructure (implementações)
- **Não conhece**: Presentation (REST, CLI, etc.)

### Princípios Aplicados

1. **Use Case Pattern**: Cada caso de uso é uma classe separada
2. **CQRS**: Commands para mudanças de estado
3. **Dependency Inversion**: Depende apenas de portas (interfaces)
4. **Single Responsibility**: Cada use case tem uma responsabilidade única

## 📦 Padrões Utilizados

### 1. Use Case Pattern

Cada caso de uso encapsula uma ação que o sistema pode executar.

**Benefícios:**
- Testabilidade
- Reutilização
- Clareza de intenção

### 2. Command Pattern (CQRS)

Commands representam intenções de mudança de estado.

**Benefícios:**
- Separação de dados de entrada e modelo de domínio
- Validação centralizada
- Flexibilidade

### 3. Saga Pattern (Orquestração)

Orquestra múltiplas operações em sequência com compensação.

**Benefícios:**
- Consistência eventual
- Observabilidade
- Compensação automática

## ✅ Critérios de Conclusão

- [x] Use cases implementados e testados
- [x] Lógica de negócio isolada no domínio
- [x] Commands definidos
- [x] Saga Pattern implementado
- [x] Tratamento de exceções de domínio
- [x] Testes unitários dos use cases

## 📚 Próximos Passos

- **Fase 4**: Implementar camada Infrastructure (persistência)
- **Fase 5**: Implementar camada Infrastructure (gateway de pagamento)

