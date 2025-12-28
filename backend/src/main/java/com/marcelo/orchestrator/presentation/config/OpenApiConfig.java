package com.marcelo.orchestrator.presentation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI smartOrderOrchestratorOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Smart Order Orchestrator API")
                .description("""
                    API REST para gerenciamento de pedidos com arquitetura hexagonal.
                    
                    ## Características:
                    - **Saga Pattern**: Orquestração completa de pedidos (criar → pagar → analisar risco)
                    - **Arquitetura Hexagonal**: Separação clara de responsabilidades
                    - **Resiliência**: Circuit Breaker e Retry para integrações externas
                    - **Observabilidade**: Rastreamento completo de execuções
                    
                    ## Fluxo de Pedido:
                    1. Criar pedido (status: PENDING)
                    2. Processar pagamento (status: PAID ou PAYMENT_FAILED)
                    3. Analisar risco (riskLevel: LOW, HIGH, ou PENDING)
                    
                    ## Integrações:
                    - **AbacatePay**: Gateway de pagamento
                    - **OpenAI**: Análise de risco com IA
                    """)
                .version("v1")
                .contact(new Contact()
                    .name("Marcelo Hernandes")
                    .email("marcelohs40@gmail.com")
                    .url("https://www.linkedin.com/in/marcelo-hernandes-351a7159"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor de Desenvolvimento"),
                new Server()
                    .url("https://api.smartorder.com")
                    .description("Servidor de Produção")
            ));
    }
}

