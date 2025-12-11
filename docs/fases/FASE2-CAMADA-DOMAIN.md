# Fase 2: Camada Domain (Core)

## 🎯 Objetivo

Implementar modelos de domínio e regras de negócio seguindo Domain-Driven Design (DDD), garantindo que o domínio seja independente de frameworks e tecnologias.

## ✅ Entregas

### 1. Entidades de Domínio

#### Order (Entidade Rica)

**Características:**
- **Rich Domain Model**: Regras de negócio encapsuladas na entidade
- **Sem JPA**: Domínio puro, sem dependências de infraestrutura
- **Imutabilidade Parcial**: Alguns campos imutáveis (id, orderNumber), outros mutáveis (status, paymentId)

**Métodos de Negócio:**
- `calculateTotal()`: Calcula total baseado nos itens
- `updateStatus(OrderStatus)`: Atualiza status com validação de transição
- `markAsPaid(String)`: Marca pedido como pago
- `markAsPaymentFailed()`: Marca falha de pagamento
- `updateRiskLevel(RiskLevel)`: Atualiza nível de risco

**Por que Rich Domain Model?**
- **Encapsulamento**: Regras de negócio próximas aos dados
- **Coesão**: Tudo relacionado a um pedido em um único lugar
- **Testabilidade**: Testável sem dependências externas
- **Sem Anemia**: Evita "Anemic Domain Model" (entidades vazias)

#### Customer (Entidade - Uso Futuro)

**Características:**
- Representa um cliente do sistema (entidade completa)
- Métodos de validação: `hasValidEmail()`, `hasCompleteAddress()`
- **Nota Importante:** Atualmente, a entidade `Order` não usa diretamente `Customer` como relacionamento. 
  Em vez disso, armazena um **snapshot** dos dados do cliente (`customerId`, `customerName`, `customerEmail`) 
  diretamente no pedido para manter histórico imutável.

**Por que Snapshot no Order?**
- **Histórico Imutável:** Dados do cliente no momento da compra são preservados mesmo se o cliente atualizar depois
- **Desacoplamento:** Order não depende de Customer estar disponível para ser consultado
- **Performance:** Evita joins desnecessários ao consultar pedidos
- **Auditoria:** Garante que o pedido sempre reflete os dados exatos do momento da compra

**Uso Futuro:**
- `Customer` e `Address` estão implementados e podem ser usados em futuras funcionalidades
- Exemplo: Consulta de histórico de pedidos do cliente, análise de comportamento, etc.

#### OrderItem (Value Object)

**Características:**
- **Imutável**: Uma vez criado, não pode ser alterado
- **Value Object**: Definido por seus atributos, não por identidade
- **Método de Negócio**: `getSubtotal()` calcula subtotal do item

**Por que Value Object?**
- **Imutabilidade**: Thread-safe, previne bugs de estado compartilhado
- **Encapsulamento**: Lógica de cálculo no próprio objeto
- **Sem JPA**: Objeto de domínio puro

### 2. Value Objects

#### Money

**Características:**
- Representa valores monetários com precisão (`BigDecimal`)
- Inclui moeda (ex: "BRL")
- Métodos: `add()`, `subtract()`, `multiply()`, `isZero()`, `isPositive()`

**Por que Value Object?**
- **Precisão**: `BigDecimal` evita problemas de arredondamento
- **Imutabilidade**: Valores monetários não devem mudar
- **Type Safety**: Evita passar valores primitivos incorretos

#### OrderNumber

**Características:**
- Gera números únicos de pedido (ex: "ORD-1234567890")
- Validação de formato
- Factory method: `OrderNumber.generate()`

**Por que Value Object?**
- **Validação**: Garante formato correto
- **Imutabilidade**: Número do pedido não muda
- **Encapsulamento**: Lógica de geração no próprio objeto

#### Address (Value Object - Uso Futuro)

**Características:**
- Representa endereço completo
- Método: `isComplete()` valida se endereço está completo
- Método: `getFullAddress()` retorna endereço formatado
- **Nota Importante:** Atualmente usado em `Customer`, mas pode ser expandido para 
  endereço de entrega em `Order` em futuras implementações

### 3. Enums (State Machine)

#### OrderStatus

**Características:**
- **State Machine Pattern**: Controla transições de estado
- Métodos: `getAllowedTransitions()`, `canTransitionTo()`

**Estados:**
- `PENDING`: Pedido criado, aguardando pagamento
- `PAID`: Pagamento confirmado
- `PAYMENT_FAILED`: Falha no pagamento
- `CANCELED`: Pedido cancelado

**Por que State Machine no Enum?**
- **Encapsulamento**: Regras de transição no próprio enum
- **Type Safety**: Compilador garante estados válidos
- **Imutabilidade**: Estados são constantes
- **Testabilidade**: Fácil testar transições

#### RiskLevel

**Estados:**
- `PENDING`: Análise ainda não realizada
- `LOW`: Risco baixo
- `HIGH`: Risco alto

### 4. Ports (Interfaces)

#### OrderRepositoryPort

**Responsabilidade:**
- Define contrato para persistência de pedidos
- Métodos: `save()`, `findById()`, `findByOrderNumber()`, `findAll()`, `findByStatus()`, `deleteById()`, `existsById()`

**Por que Port?**
- **Inversão de Dependência**: Domínio define contrato, infraestrutura implementa
- **Testabilidade**: Fácil mockar para testes
- **Flexibilidade**: Pode trocar implementação (JPA, MongoDB, etc.)

#### PaymentGatewayPort

**Responsabilidade:**
- Define contrato para processamento de pagamentos
- Métodos: `processPayment(PaymentRequest)`

#### PaymentStatus (Enum - Parte do PaymentGatewayPort)

**Localização:** `domain.port.PaymentStatus` (não em `domain.model`)

**Estados:**
- `PENDING`: Pagamento pendente
- `SUCCESS`: Pagamento bem-sucedido
- `FAILED`: Pagamento falhou

**Por que no Port e não no Model?**
- **Encapsulamento:** PaymentStatus é parte do contrato de PaymentGatewayPort
- **Coesão:** Fica junto com PaymentRequest e PaymentResult que também são parte do port
- **Separação:** Status de pagamento é conceito de integração, não de domínio puro

#### RiskAnalysisPort

**Responsabilidade:**
- Define contrato para análise de risco
- Métodos: `analyzeRisk(RiskAnalysisRequest)`

#### NotificationPort

**Responsabilidade:**
- Define contrato para notificações
- Métodos: `notifyOrderCreated()`, `notifyOrderStatusChanged()`, `notifyPaymentFailed()`

## 🏗️ Princípios DDD Aplicados

### 1. Rich Domain Model

Regras de negócio encapsuladas nas entidades, não em services externos.

**Exemplo:**
```java
// Regra de negócio no domínio
public void updateStatus(OrderStatus newStatus) {
    if (!status.canTransitionTo(newStatus)) {
        throw new InvalidOrderStatusException(...);
    }
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();
}
```

### 2. Value Objects

Objetos imutáveis definidos por seus atributos.

**Exemplo:**
```java
// Value Object imutável
public final class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 3. Entities vs Value Objects

- **Entity**: Tem identidade única (UUID), pode mudar ao longo do tempo
- **Value Object**: Definido por atributos, imutável

### 4. Ports and Adapters

- **Port**: Interface definida no domínio
- **Adapter**: Implementação na infraestrutura

## 📦 Estrutura de Pacotes

```
domain/
├── model/
│   ├── Order.java              # Entidade rica
│   ├── OrderItem.java          # Value Object
│   ├── Customer.java           # Entidade (uso futuro)
│   ├── Address.java            # Value Object (uso futuro)
│   ├── Money.java              # Value Object
│   ├── OrderNumber.java        # Value Object
│   ├── OrderStatus.java        # Enum (State Machine)
│   └── RiskLevel.java          # Enum
└── port/
    ├── OrderRepositoryPort.java
    ├── PaymentGatewayPort.java
    ├── PaymentStatus.java       # Enum (parte do PaymentGatewayPort)
    ├── PaymentRequest.java      # DTO (parte do PaymentGatewayPort)
    ├── PaymentResult.java       # DTO (parte do PaymentGatewayPort)
    ├── RiskAnalysisPort.java
    ├── RiskAnalysisRequest.java # DTO (parte do RiskAnalysisPort)
    ├── RiskAnalysisResult.java  # DTO (parte do RiskAnalysisPort)
    ├── EventPublisherPort.java
    └── NotificationPort.java
```

## ✅ Critérios de Conclusão

- [x] Modelos de domínio sem dependências externas
- [x] Portas definidas e documentadas
- [x] Testes unitários dos modelos
- [x] Rich Domain Model implementado
- [x] Value Objects imutáveis
- [x] State Machine nos enums

## 📚 Próximos Passos

- **Fase 3**: Implementar camada Application (use cases)
- **Fase 4**: Implementar camada Infrastructure (persistência)

