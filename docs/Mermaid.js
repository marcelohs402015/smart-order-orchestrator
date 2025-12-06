graph TD
    %% --- DEFINIÇÃO DE CORES E ESTILOS ---
    classDef domain fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,color:#0d47a1;
    classDef app fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#1b5e20;
    classDef infra fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,color:#e65100;
    classDef present fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#4a148c;
    
    %% --- SISTEMAS EXTERNOS ---
    subgraph External_Systems [External Systems]
        Client([User / Frontend])
        DB[(PostgreSQL)]
        AbacatePay[AbacatePay API]
        OpenAI[OpenAI API]
    end

    %% --- CAMADA DE APRESENTAÇÃO ---
    subgraph Presentation_Layer [Presentation Layer]
        Controller[OrderController]:::present
        DTOs[DTOs & Mappers]:::present
    end

    %% --- CAMADA DE APLICAÇÃO ---
    subgraph Application_Layer [Application Layer]
        Orchestrator[OrderSagaOrchestrator]:::app
        UC_Create[CreateOrderUseCase]:::app
        UC_Pay[ProcessPaymentUseCase]:::app
        UC_Risk[AnalyzeRiskUseCase]:::app
        UC_Compensate[Compensate / Cancel]:::app
    end

    %% --- CAMADA DE DOMÍNIO (CORE) ---
    subgraph Domain_Layer [Domain Layer - The Core]
        Entity_Order[Order Entity]:::domain
        VOs[Value Objects]:::domain
        
        subgraph Ports [Ports / Interfaces]
            Port_Repo[OrderRepositoryPort]:::domain
            Port_Pay[PaymentGatewayPort]:::domain
            Port_Risk[RiskAnalysisPort]:::domain
        end
    end

    %% --- CAMADA DE INFRAESTRUTURA ---
    subgraph Infrastructure_Layer [Infrastructure Layer]
        Adapter_Repo[OrderRepositoryAdapter]:::infra
        Adapter_Pay[AbacatePayAdapter]:::infra
        Adapter_Risk[OpenAIRiskAnalysisAdapter]:::infra
        Resilience[Resilience4j]:::infra
    end

    %% --- RELACIONAMENTOS ---
    
    %% Fluxo Principal
    Client -->|POST /orders| Controller
    Controller -->|Calls| Orchestrator
    
    %% Saga
    Orchestrator -->|Step 1| UC_Create
    Orchestrator -->|Step 2| UC_Pay
    Orchestrator -->|Step 3| UC_Risk
    Orchestrator -.->|Failure| UC_Compensate

    %% Conexões com Domínio
    UC_Create & UC_Pay & UC_Risk --> Entity_Order
    UC_Create --> Port_Repo
    UC_Pay --> Port_Pay
    UC_Risk --> Port_Risk

    %% Implementação dos Adapters
    Adapter_Repo -.->|Implements| Port_Repo
    Adapter_Pay -.->|Implements| Port_Pay
    Adapter_Risk -.->|Implements| Port_Risk

    %% Conexões Externas
    Adapter_Repo -->|JPA| DB
    Adapter_Pay -->|WebClient| Resilience
    Adapter_Risk -->|WebClient| Resilience
    Resilience --> AbacatePay
    Resilience --> OpenAI