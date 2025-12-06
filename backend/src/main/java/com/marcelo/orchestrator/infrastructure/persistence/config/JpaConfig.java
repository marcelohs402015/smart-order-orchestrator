package com.marcelo.orchestrator.infrastructure.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuração JPA para a camada de persistência.
 * 
 * <p>Habilita funcionalidades do Spring Data JPA e configurações de transação.</p>
 * 
 * <h3>Anotações:</h3>
 * <ul>
 *   <li><strong>@EnableJpaRepositories:</strong> Habilita repositórios Spring Data JPA</li>
 *   <li><strong>@EnableJpaAuditing:</strong> Habilita auditoria JPA (@CreatedDate, @LastModifiedDate)</li>
 *   <li><strong>@EnableTransactionManagement:</strong> Habilita gerenciamento de transações</li>
 * </ul>
 * 
 * <h3>Pacote de Repositórios:</h3>
 * <p>Define onde Spring Data JPA deve procurar interfaces de repositório.
 * Isso mantém organização e permite múltiplos módulos de persistência.</p>
 * 
 * @author Marcelo
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.marcelo.orchestrator.infrastructure.persistence.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // Configuração adicional pode ser adicionada aqui se necessário
}

