package com.marcelo.orchestrator.infrastructure.messaging.config;

import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.infrastructure.messaging.factory.EventPublisherFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Spring para EventPublisherPort.
 * 
 * <p>Implementa o padrão <strong>Configuration Pattern</strong> do Spring,
 * centralizando a configuração de beans relacionados a publicação de eventos.</p>
 * 
 * <h3>Padrão: Configuration Pattern (Spring)</h3>
 * <ul>
 *   <li><strong>Centralização:</strong> Toda configuração de eventos em um lugar</li>
 *   <li><strong>Injeção de Dependências:</strong> Spring gerencia ciclo de vida</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar em testes</li>
 * </ul>
 * 
 * <h3>Como Funciona:</h3>
 * <ol>
 *   <li>Spring detecta esta classe (@Configuration)</li>
 *   <li>Injeta EventPublisherFactory (que já tem todos os adapters)</li>
 *   <li>Cria bean EventPublisherPort usando factory.create()</li>
 *   <li>Bean fica disponível para injeção em qualquer lugar</li>
 * </ol>
 * 
 * <h3>Uso:</h3>
 * <pre>{@code
 * @Service
 * public class OrderSagaOrchestrator {
 *     private final EventPublisherPort eventPublisher; // Injetado automaticamente
 * }
 * }</pre>
 * 
 * @author Marcelo
 */
@Configuration
@RequiredArgsConstructor
public class EventPublisherConfig {
    
    private final EventPublisherFactory eventPublisherFactory;
    
    /**
     * Cria bean EventPublisherPort usando Factory Pattern.
     * 
     * <p>Padrão: Factory Method (Spring Bean) - Spring chama este método
     * uma vez e reutiliza o bean em toda aplicação.</p>
     * 
     * <p>Padrão: Singleton - Spring gerencia como singleton por padrão,
     * garantindo que apenas uma instância seja criada.</p>
     * 
     * @return Instância de EventPublisherPort configurada
     */
    @Bean
    public EventPublisherPort eventPublisher() {
        return eventPublisherFactory.create();
    }
}

