graph TD
    %% --- DEFINIÇÃO DE CORES E ESTILOS ---
    classDef domain fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,color:#0d47a1;
    classDef app fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#1b5e20;
    classDef infra fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,color:#e65100;
    classDef present fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#4a148c;
    classDef mcp fill:#e1f5fe,stroke:#0277bd,stroke-width:2px,color:#01579b;
    classDef thread fill:#f1f8e9,stroke:#558b2f,stroke-width:2px,color:#33691e;
    
    %% --- SISTEMAS EXTERNOS ---
    subgraph External_Systems [External Systems]
        Client([User / Frontend])
        DB[(PostgreSQL)]
        AbacatePay[AbacatePay API]
        OpenAI[OpenAI API]
        Claude[Claude / GPT-4<br/>GitHub Copilot]:::mcp
    end

    %% --- CAMADA DE APRESENTAÇÃO ---
    subgraph Presentation_Layer [Presentation Layer]
        Controller[OrderController]:::present
        DTOs[DTOs & Mappers]:::present
    end

    %% --- CAMADA DE APLICAÇÃO ---
    subgraph Application_Layer [Application Layer]
        Orchestrator[OrderSagaOrchestrator<br/>+ Idempotency Check<br/>+ Event Publishing]:::app
        UC_Create[CreateOrderUseCase]:::app
        UC_Pay[ProcessPaymentUseCase]:::app
        UC_Risk[AnalyzeRiskUseCase]:::app
        UC_Compensate[Compensate / Cancel]:::app
        SagaExecution[SagaExecutionEntity<br/>Observability]:::app
    end

    %% --- CAMADA DE DOMÍNIO (CORE) ---
    subgraph Domain_Layer [Domain Layer - The Core]
        Entity_Order[Order Entity]:::domain
        VOs[Value Objects]:::domain
        
        subgraph Ports [Ports / Interfaces]
            Port_Repo[OrderRepositoryPort]:::domain
            Port_Pay[PaymentGatewayPort]:::domain
            Port_Risk[RiskAnalysisPort]:::domain
            Port_Event[EventPublisherPort]:::domain
        end
        
        subgraph Domain_Events [Domain Events]
            Event_OrderCreated[OrderCreatedEvent]:::domain
            Event_PaymentProcessed[PaymentProcessedEvent]:::domain
            Event_SagaCompleted[SagaCompletedEvent]:::domain
            Event_SagaFailed[SagaFailedEvent]:::domain
        end
    end

    %% --- CAMADA DE INFRAESTRUTURA ---
    subgraph Infrastructure_Layer [Infrastructure Layer]
        Adapter_Repo[OrderRepositoryAdapter]:::infra
        Adapter_Pay[AbacatePayAdapter]:::infra
        Adapter_Risk[OpenAIRiskAnalysisAdapter]:::infra
        Resilience[Resilience4j<br/>Circuit Breaker<br/>Retry + Fallback]:::infra
        
        subgraph Event_System [Event-Driven Architecture]
            EventFactory[EventPublisherFactory<br/>Factory Pattern]:::infra
            EventPort_Impl[EventPublisherPort<br/>Implementation]:::infra
            KafkaAdapter[KafkaEventPublisherAdapter]:::infra
            PubSubAdapter[PubSubEventPublisherAdapter]:::infra
            RabbitAdapter[RabbitMqEventPublisherAdapter]:::infra
            InMemoryAdapter[InMemoryEventPublisherAdapter]:::infra
        end
        
        VirtualThreads[Virtual Threads<br/>Java 21<br/>Performance Config]:::thread
    end

    %% --- MESSAGE BROKERS ---
    subgraph Message_Brokers [Message Brokers]
        Kafka[Kafka / Pub/Sub<br/>RabbitMQ]:::infra
    end

    %% --- MCP CODE REVIEW SERVER ---
    subgraph MCP_Server [MCP Code Review Server]
        McpServer[McpServer<br/>JSON-RPC 2.0]:::mcp
        McpController[McpController<br/>REST API]:::mcp
        CodeReviewTool[CodeReviewTool]:::mcp
        PatternAnalysisTool[PatternAnalysisTool]:::mcp
        CodeAnalyzer[CodeAnalyzer<br/>JavaParser AST]:::mcp
        PatternDetector[PatternDetector<br/>Design Patterns + SOLID]:::mcp
        AiFeedbackService[AiFeedbackService<br/>OpenAI GPT-4]:::mcp
    end

    %% --- RELACIONAMENTOS ---
    
    %% Fluxo Principal com Idempotência
    Client -->|POST /orders<br/>idempotencyKey| Controller
    Controller -->|Calls| Orchestrator
    
    %% Saga com Idempotência e Observabilidade
    Orchestrator -->|Step 1: Create<br/>+ Idempotency Check| UC_Create
    Orchestrator -->|Step 2: Payment<br/>+ Circuit Breaker| UC_Pay
    Orchestrator -->|Step 3: Risk Analysis<br/>+ Circuit Breaker| UC_Risk
    Orchestrator -.->|Failure| UC_Compensate
    Orchestrator -->|Tracks Execution| SagaExecution

    %% Event-Driven: Publicação de Eventos
    Orchestrator -->|Publishes Events| EventFactory
    EventFactory -->|Factory Pattern| EventPort_Impl
    EventPort_Impl --> KafkaAdapter
    EventPort_Impl --> PubSubAdapter
    EventPort_Impl --> RabbitAdapter
    EventPort_Impl --> InMemoryAdapter
    KafkaAdapter -->|Domain Events| Kafka
    PubSubAdapter -->|Domain Events| Kafka
    RabbitAdapter -->|Domain Events| Kafka
    
    %% Domain Events
    Orchestrator -->|Creates| Event_OrderCreated
    Orchestrator -->|Creates| Event_PaymentProcessed
    Orchestrator -->|Creates| Event_SagaCompleted
    Orchestrator -->|Creates| Event_SagaFailed
    Event_OrderCreated --> EventFactory
    Event_PaymentProcessed --> EventFactory
    Event_SagaCompleted --> EventFactory
    Event_SagaFailed --> EventFactory

    %% Conexões com Domínio
    UC_Create & UC_Pay & UC_Risk --> Entity_Order
    UC_Create --> Port_Repo
    UC_Pay --> Port_Pay
    UC_Risk --> Port_Risk
    Orchestrator --> Port_Event

    %% Implementação dos Adapters
    Adapter_Repo -.->|Implements| Port_Repo
    Adapter_Pay -.->|Implements| Port_Pay
    Adapter_Risk -.->|Implements| Port_Risk
    EventPort_Impl -.->|Implements| Port_Event

    %% Conexões Externas
    Adapter_Repo -->|JPA| DB
    Adapter_Pay -->|WebClient| Resilience
    Adapter_Risk -->|WebClient| Resilience
    Resilience --> AbacatePay
    Resilience --> OpenAI
    
    %% Virtual Threads
    Controller -->|Uses| VirtualThreads
    Orchestrator -->|Uses| VirtualThreads
    Adapter_Pay -->|Uses| VirtualThreads
    Adapter_Risk -->|Uses| VirtualThreads
    
    %% MCP Code Review Server
    Claude -->|JSON-RPC 2.0| McpServer
    McpServer --> McpController
    McpController --> CodeReviewTool
    McpController --> PatternAnalysisTool
    CodeReviewTool --> CodeAnalyzer
    CodeReviewTool --> AiFeedbackService
    PatternAnalysisTool --> PatternDetector
    PatternAnalysisTool --> AiFeedbackService
    AiFeedbackService -->|API Call| OpenAI
    CodeAnalyzer -->|AST Analysis| Entity_Order
    PatternDetector -->|Pattern Detection| Entity_Order