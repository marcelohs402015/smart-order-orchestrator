# Integração com AbacatePay

Documentação da integração com o gateway de pagamento AbacatePay.

## 📚 Documentação Oficial

- [Introdução AbacatePay](https://docs.abacatepay.com/pages/introduction)
- [API Reference - Criar Cliente](https://docs.abacatepay.com/api-reference/criar-um-novo-cliente)
- [API Reference - Criar Cobrança](https://docs.abacatepay.com/api-reference/criar-uma-nova-cobrança)

## 🔑 Configuração

### Variáveis de Ambiente

Configure a chave de API do AbacatePay:

```bash
export ABACATEPAY_API_KEY=sua_chave_api_aqui
```

Ou no `application.properties`:

```properties
abacatepay.api.key=sua_chave_api_aqui
```

### Base URL

A URL base padrão é: `https://api.abacatepay.com/v1`

Pode ser sobrescrita via variável de ambiente:

```bash
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
```

## 🏗️ Arquitetura da Integração

### Componentes

1. **AbacatePayAdapter**: Implementa `PaymentGatewayPort`
2. **AbacatePayConfig**: Configura `WebClient` para chamadas HTTP
3. **DTOs**: `AbacatePayBillingRequest`, `AbacatePayBillingResponse`

### Fluxo

```
Use Case (ProcessPaymentUseCase)
    ↓
PaymentGatewayPort (interface)
    ↓
AbacatePayAdapter (implementação)
    ↓
WebClient → AbacatePay API
    ↓
PaymentResult (domínio)
```

## 🔒 Resiliência

A integração utiliza Resilience4j:

- **Circuit Breaker**: Protege contra falhas em cascata
- **Retry**: Tenta novamente em falhas transitórias
- **Fallback**: Retorna resultado com falha quando gateway indisponível

## 📝 Endpoints Utilizados

### Criar Cobrança

- **Endpoint**: `POST /v1/billing/create`
- **Autenticação**: Bearer token
- **Request**: `AbacatePayBillingRequest`
- **Response**: `AbacatePayBillingResponse`

### Status da Cobrança

- **Endpoint**: `GET /v1/billing/get/{id}` (a implementar)
- **Autenticação**: Bearer token

## 🧪 Testes

Execute os testes unitários:

```bash
mvn test -Dtest=AbacatePayAdapterTest
```

## ⚠️ Notas Importantes

1. **Valores em Centavos**: AbacatePay trabalha com valores inteiros (centavos)
2. **Dev Mode**: Use chave de API de desenvolvimento para testes
3. **Idempotência**: API do AbacatePay é idempotente (seguro reexecutar)

