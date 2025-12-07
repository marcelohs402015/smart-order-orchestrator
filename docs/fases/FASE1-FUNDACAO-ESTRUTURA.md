# Fase 1: Fundação e Estrutura Base

## 🎯 Objetivo

Configurar o projeto e estabelecer a estrutura base da Arquitetura Hexagonal, preparando o ambiente para desenvolvimento.

## ✅ Entregas

### 1. Configuração Maven (pom.xml)

**Tecnologias Principais:**
- **Java 21**: Suporte a Virtual Threads (Project Loom) para alta concorrência
- **Spring Boot 3.2+**: Framework moderno com suporte nativo a Virtual Threads
- **Lombok**: Redução de boilerplate (getters, setters, builders)
- **MapStruct**: Geração de código para mappers entre camadas
- **Resilience4j**: Circuit Breaker, Retry e Fallback para resiliência
- **Spring Data JPA**: Persistência com PostgreSQL
- **Flyway**: Migrations versionadas do banco de dados
- **Spring WebFlux**: WebClient para chamadas HTTP reativas

**Por que estas escolhas?**
- **Java 21**: Virtual Threads permitem milhares de requisições simultâneas com baixo consumo de memória
- **Spring Boot 3.2+**: Framework enterprise com autoconfiguração e suporte a tecnologias modernas
- **Lombok**: Reduz código boilerplate mantendo legibilidade
- **MapStruct**: Mapeamento type-safe e performático entre camadas
- **Resilience4j**: Padrão enterprise para resiliência em sistemas distribuídos

### 2. Estrutura de Pacotes (Arquitetura Hexagonal)

```
com.marcelo.orchestrator/
├── domain/              # Camada de Domínio (Core)
│   ├── model/          # Entidades e Value Objects
│   └── port/           # Portas (interfaces)
├── application/        # Camada de Aplicação
│   ├── usecase/        # Casos de uso
│   ├── saga/           # Saga Pattern (orquestração)
│   └── exception/      # Exceções de domínio
├── infrastructure/      # Camada de Infraestrutura
│   ├── persistence/    # JPA, Repositories, Mappers
│   ├── payment/        # Adaptador AbacatePay
│   └── ai/             # Adaptador OpenAI
└── presentation/        # Camada de Apresentação (REST)
```

**Por que esta estrutura?**
- **Separação de Concerns**: Cada camada tem responsabilidade única
- **Independência do Domínio**: Domínio não conhece frameworks
- **Testabilidade**: Fácil testar cada camada isoladamente
- **Manutenibilidade**: Mudanças em uma camada não afetam outras

### 3. Configurações (application.yml)

**Perfis Configurados:**
- **default**: PostgreSQL em produção
- **dev**: H2 in-memory para desenvolvimento
- **prod**: Configurações otimizadas para produção

**Configurações Principais:**
- **Virtual Threads**: Habilitado (`spring.threads.virtual.enabled=true`)
- **JPA/Hibernate**: Configurado para PostgreSQL
- **Flyway**: Migrations versionadas
- **Resilience4j**: Circuit Breaker e Retry configurados
- **Actuator**: Health checks e métricas

### 4. Docker Compose

**PostgreSQL** configurado para desenvolvimento local:
- Porta: 5432
- Database: smartorder
- Usuário: postgres
- Scripts de inicialização em `scripts/init-scripts/`

### 5. Health Checks (Actuator)

**Endpoints Disponíveis:**
- `/actuator/health`: Status da aplicação
- `/actuator/info`: Informações da aplicação
- `/actuator/metrics`: Métricas da aplicação
- `/actuator/prometheus`: Métricas no formato Prometheus

## 🏗️ Arquitetura Hexagonal

### Princípios Aplicados

1. **Ports and Adapters**: Domínio define portas (interfaces), infraestrutura implementa adaptadores
2. **Dependency Inversion**: Domínio não depende de implementações
3. **Separation of Concerns**: Cada camada tem responsabilidade única
4. **Testability**: Domínio testável sem frameworks

### Fluxo de Dependências

```
Presentation → Application → Domain ← Infrastructure
```

- **Presentation** depende de **Application**
- **Application** depende de **Domain**
- **Infrastructure** implementa portas definidas em **Domain**
- **Domain** não depende de nada (core isolado)

## 📦 Dependências Principais

### Core
- `spring-boot-starter-web`: REST API
- `spring-boot-starter-data-jpa`: Persistência
- `spring-boot-starter-validation`: Validação
- `spring-boot-starter-actuator`: Health checks e métricas

### Persistência
- `postgresql`: Driver PostgreSQL
- `h2`: Banco in-memory para testes
- `flyway-core`: Migrations

### Resiliência
- `resilience4j-spring-boot3`: Circuit Breaker e Retry
- `resilience4j-reactor`: Integração com WebFlux

### Utilitários
- `lombok`: Redução de boilerplate
- `mapstruct`: Mapeamento entre camadas
- `spring-boot-starter-webflux`: WebClient para HTTP reativo

## 🚀 Como Rodar

### 1. Iniciar PostgreSQL (Docker)

```bash
cd scripts
docker-compose up -d
```

### 2. Configurar Variáveis de Ambiente

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/smartorder
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
```

### 3. Executar Aplicação

```bash
mvn spring-boot:run
```

### 4. Verificar Health

```bash
curl http://localhost:8080/actuator/health
```

## ✅ Critérios de Conclusão

- [x] Projeto compila sem erros
- [x] Conexão com PostgreSQL funcionando
- [x] Health endpoint respondendo
- [x] Estrutura de pacotes organizada
- [x] Configurações de ambiente funcionando
- [x] Docker Compose configurado

## 📚 Próximos Passos

- **Fase 2**: Implementar camada Domain (entidades, value objects, ports)
- **Fase 3**: Implementar camada Application (use cases)
- **Fase 4**: Implementar camada Infrastructure (persistência)

