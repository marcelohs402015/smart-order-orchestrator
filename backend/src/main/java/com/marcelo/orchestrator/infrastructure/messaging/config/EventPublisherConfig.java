package com.marcelo.orchestrator.infrastructure.messaging.config;

import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.infrastructure.messaging.factory.EventPublisherFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class EventPublisherConfig {
    
    private final EventPublisherFactory eventPublisherFactory;
    
    
    @Bean
    public EventPublisherPort eventPublisher() {
        return eventPublisherFactory.create();
    }
}

