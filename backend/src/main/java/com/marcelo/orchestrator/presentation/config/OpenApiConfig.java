package com.marcelo.orchestrator.presentation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API REST.
 * 
 * <p>Define informações da API, servidores e configurações gerais
 * para geração automática da documentação interativa.</p>
 * 
 * <h3>Acesso à Documentação:</h3>
 * <ul>
 *   <li><strong>Swagger UI:</strong> http://localhost:8080/swagger-ui.html</li>
 *   <li><strong>OpenAPI JSON:</strong> http://localhost:8080/v3/api-docs</li>
 * </ul>
 * 
 * <h3>Por que Swagger/OpenAPI?</h3>
 * <ul>
 *   <li><strong>Documentação Automática:</strong> Gera documentação a partir de anotações</li>
 *   <li><strong>Teste Interativo:</strong> Permite testar API diretamente do navegador</li>
 *   <li><strong>Contrato de API:</strong> Define contrato claro entre cliente e servidor</li>
 *   <li><strong>Versionamento:</strong> Suporta múltiplas versões da API</li>
 * </ul>
 * 
 * @author Marcelo
 */
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

