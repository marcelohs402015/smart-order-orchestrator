package com.marcelo.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Smart Order Orchestrator.
 * 
 * <p>Esta aplicação demonstra uma arquitetura de referência para sistemas de alta resiliência
 * utilizando Arquitetura Hexagonal (Ports and Adapters) e Clean Architecture.</p>
 * 
 * <h3>Decisões Arquiteturais:</h3>
 * <ul>
 *   <li><strong>Arquitetura Hexagonal:</strong> Isolamento do domínio das tecnologias,
 *       facilitando testes, manutenção e troca de implementações.</li>
 *   <li><strong>Spring Boot 3.2+:</strong> Escolhido por suporte nativo a Virtual Threads
 *       (Project Loom) e melhor performance comparado a versões anteriores.</li>
 * </ul>
 * 
 * <h3>Recursos Habilitados:</h3>
 * <ul>
 *   <li><strong>Virtual Threads:</strong> Habilitadas via {@code spring.threads.virtual.enabled=true}
 *       para alta concorrência sem overhead de threads tradicionais (Platform Threads).</li>
 *   <li><strong>Actuator:</strong> Health checks e métricas disponíveis em {@code /actuator}.</li>
 * </ul>
 * 
 * <h3>Estrutura de Camadas:</h3>
 * <ul>
 *   <li><strong>Domain:</strong> Regras de negócio puras, sem dependências de frameworks.</li>
 *   <li><strong>Application:</strong> Casos de uso e orquestração (Use Cases).</li>
 *   <li><strong>Infrastructure:</strong> Adaptadores para persistência, APIs externas, etc.</li>
 *   <li><strong>Presentation:</strong> Controllers REST, DTOs, validações.</li>
 * </ul>
 * 
 * @author Marcelo
 * @version 1.0.0
 */
@SpringBootApplication
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}

