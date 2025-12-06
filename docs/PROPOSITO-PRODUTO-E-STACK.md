# Propósito do Produto e Stack Tecnológica

## 🎯 Propósito do Produto

### Smart Order Orchestrator

**O que é:**
Sistema orquestrador de pedidos resiliente que demonstra práticas avançadas de engenharia de software para sistemas distribuídos, microserviços e integrações com serviços externos.

**Problema de Negócio que Resolve:**
Em sistemas distribuídos, especialmente em e-commerce, é comum ter múltiplas integrações externas (gateways de pagamento, análise de risco, notificações). O sistema precisa:
- Garantir consistência de dados mesmo com falhas em integrações
- Oferecer degradação graciosa quando serviços externos estão indisponíveis
- Utilizar IA para modernizar tomada de decisão (análise de risco)
- Escalar para cenários de alta carga (Black Friday, promoções)

**Cenário de Uso Real:**
1. Cliente faz pedido no e-commerce
2. Sistema cria pedido e processa pagamento (AbacatePay)
3. Se pagamento aprovado, IA analisa risco do pedido (OpenAI)
4. Sistema notifica cliente e processa pedido
5. Tudo rastreado e observável para debugging e métricas

## 🏗️ Arquitetura Escolhida

### Arquitetura Hexagonal (Ports and Adapters)

**Por que esta arquitetura?**
- **Isolamento do Domínio**: Regras de negócio não dependem de frameworks
- **Testabilidade**: Fácil testar cada camada isoladamente
- **Flexibilidade**: Pode trocar implementações (JPA → MongoDB, REST → GraphQL)
- **Manutenibilidade**: Mudanças em uma camada não afetam outras
- **Padrão Enterprise**: Usado em sistemas críticos e microserviços

**Benefícios para Entrevista:**
- Demonstra conhecimento de arquitetura de software
- Mostra que entende separação de concerns
- Alinhado com práticas de clientes enterprise (Accenture)

### Clean Architecture

**Princípios Aplicados:**
- **Dependency Inversion**: Domínio não depende de implementações
- **Single Responsibility**: Cada classe tem uma responsabilidade
- **Open/Closed**: Aberto para extensão, fechado para modificação
- **Separation of Concerns**: Cada camada tem propósito único

## 🚀 Stack Tecnológica e Justificativas

### 1. Java 21

**Por que Java 21?**
- **Virtual Threads (Project Loom)**: Permite milhões de threads simultâneas com baixo consumo
- **LTS (Long Term Support)**: Suporte até 2029
- **Moderno**: Última versão LTS com recursos de ponta
- **Enterprise**: Padrão em sistemas críticos

**Benefício Concreto:**
- 100.000 requisições simultâneas usando ~100MB de memória (vs ~100GB com threads tradicionais)
- Ideal para sistemas I/O-bound (múltiplas integrações)

**Alinhamento com Clientes:**
- Accenture e clientes enterprise usam Java 21 em microserviços
- Tecnologia de ponta valorizada em entrevistas

### 2. Spring Boot 3.2+

**Por que Spring Boot 3.2+?**
- **Suporte Nativo a Virtual Threads**: Integração perfeita com Java 21
- **Autoconfiguração**: Reduz boilerplate e acelera desenvolvimento
- **Ecosystem Maduro**: Muitas integrações prontas (JPA, WebFlux, Actuator)
- **Enterprise Ready**: Usado em sistemas críticos de grandes empresas

**Benefício Concreto:**
- Desenvolvimento rápido com qualidade enterprise
- Suporte nativo a tecnologias modernas (Virtual Threads, WebFlux)

### 3. Arquitetura Hexagonal

**Por que esta arquitetura?**
- **Testabilidade**: Domínio testável sem frameworks
- **Flexibilidade**: Fácil trocar implementações
- **Manutenibilidade**: Código organizado e fácil de entender
- **Padrão Enterprise**: Usado em sistemas críticos

**Benefício para Entrevista:**
- Demonstra conhecimento de design patterns avançados
- Mostra que entende arquitetura de software
- Alinhado com práticas de clientes enterprise

### 4. PostgreSQL

**Por que PostgreSQL?**
- **ACID**: Garante consistência de dados
- **Robusto**: Banco enterprise usado em sistemas críticos
- **Open Source**: Sem custos de licenciamento
- **Performance**: Excelente para operações complexas

**Benefício Concreto:**
- Consistência garantida para dados críticos (pedidos, pagamentos)
- Suporte a transações complexas

### 5. Resilience4j

**Por que Resilience4j?**
- **Circuit Breaker**: Protege contra falhas em cascata
- **Retry**: Tenta novamente em falhas transitórias
- **Fallback**: Degradação graciosa quando serviços estão indisponíveis
- **Padrão Enterprise**: Usado em microserviços

**Benefício Concreto:**
- Sistema continua funcionando mesmo se AbacatePay ou OpenAI estiverem offline
- Previne falhas em cascata

### 6. Saga Pattern

**Por que Saga Pattern?**
- **Consistência Eventual**: Garante que todas as operações sejam executadas
- **Compensação**: Rollback automático se algum passo falhar
- **Observabilidade**: Rastreamento completo de cada execução
- **Padrão Enterprise**: Usado em microserviços e sistemas distribuídos

**Benefício Concreto:**
- Orquestração completa de pedidos (criar → pagar → analisar risco)
- Rastreamento completo para debugging e métricas

### 7. OpenAI (IA)

**Por que OpenAI?**
- **Modernização**: Demonstra uso de IA em sistemas enterprise
- **Análise Inteligente**: Classificação de risco mais precisa
- **Tendência**: IA é o futuro, demonstra visão de futuro
- **Diferencial**: Poucos projetos demonstram integração com IA

**Benefício Concreto:**
- Análise de risco mais inteligente
- Demonstra conhecimento de tecnologias emergentes

### 8. Virtual Threads

**Por que Virtual Threads?**
- **Alta Concorrência**: Milhões de threads simultâneas
- **Baixo Consumo**: ~1KB por thread vs ~1MB tradicional
- **Melhor I/O**: CPU não fica ociosa esperando I/O
- **Tecnologia de Ponta**: Java 21, alinhado com clientes enterprise

**Benefício Concreto:**
- Sistema pode processar 100.000+ requisições simultâneas
- Ideal para cenários de alta carga (Black Friday)

### 9. Swagger/OpenAPI

**Por que Swagger/OpenAPI?**
- **Documentação Automática**: Gera documentação a partir de código
- **Teste Interativo**: Permite testar API diretamente do navegador
- **Contrato de API**: Define contrato claro entre cliente e servidor
- **Padrão Industry**: Usado em APIs modernas

**Benefício Concreto:**
- Documentação sempre atualizada
- Facilita integração com frontend ou outros serviços

### 10. Flyway

**Por que Flyway?**
- **Versionamento**: Migrations versionadas e rastreáveis
- **Reprodutibilidade**: Mesmo schema em todos os ambientes
- **Auditoria**: Histórico completo de mudanças
- **Padrão Enterprise**: Usado em sistemas críticos

**Benefício Concreto:**
- Schema do banco versionado como código
- Fácil deploy em diferentes ambientes

## 📊 Resumo da Stack

| Tecnologia | Versão | Propósito | Benefício |
|------------|--------|-----------|-----------|
| Java | 21 | Linguagem base | Virtual Threads, LTS até 2029 |
| Spring Boot | 3.2+ | Framework | Autoconfiguração, suporte Virtual Threads |
| PostgreSQL | Latest | Banco de dados | ACID, robustez, performance |
| Resilience4j | 2.1.0 | Resiliência | Circuit Breaker, Retry, Fallback |
| Flyway | Latest | Migrations | Versionamento de schema |
| MapStruct | 1.5.5 | Mapeamento | Type-safe, performático |
| Lombok | 1.18.30 | Boilerplate | Reduz código, mantém legibilidade |
| SpringDoc | 2.3.0 | Documentação | Swagger/OpenAPI automático |
| WebFlux | 3.2+ | HTTP Reativo | WebClient para integrações |

## 🎯 Por que Esta Stack é Ideal?

### 1. **Alinhamento com Clientes Enterprise**

**Accenture e clientes enterprise usam:**
- Java 21 em microserviços
- Spring Boot para desenvolvimento rápido
- Arquitetura Hexagonal em sistemas críticos
- Virtual Threads para alta concorrência
- Resilience4j para resiliência

### 2. **Tecnologias de Ponta**

- **Java 21**: Última versão LTS com Virtual Threads
- **Spring Boot 3.2+**: Framework moderno com suporte nativo
- **IA (OpenAI)**: Demonstra visão de futuro
- **Saga Pattern**: Padrão enterprise para microserviços

### 3. **Demonstra Competências**

- **Arquitetura**: Hexagonal, Clean Architecture
- **Performance**: Virtual Threads, otimizações
- **Resiliência**: Circuit Breaker, Retry, Fallback
- **Observabilidade**: Saga Pattern, métricas
- **Modernização**: IA, tecnologias emergentes

## ✅ Conclusão

**Propósito do Produto:**
Demonstrar conhecimento avançado de engenharia de software através de um sistema real que resolve problemas de negócio reais (orquestração de pedidos com múltiplas integrações).

**Stack Escolhida:**
Tecnologias modernas, enterprise-ready, alinhadas com clientes Accenture, que demonstram competências em arquitetura, performance, resiliência e modernização.

**Diferencial:**
Combinação de arquitetura sólida (Hexagonal), tecnologias de ponta (Java 21, Virtual Threads), resiliência (Resilience4j), observabilidade (Saga Pattern) e modernização (IA).

