package com.marcelo.orchestrator.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Configuração de performance e otimizações para Virtual Threads.
 * 
 * <p>Configurações específicas para otimizar o uso de Virtual Threads
 * e garantir melhor performance em cenários de alta concorrência.</p>
 * 
 * <h3>Otimizações Aplicadas:</h3>
 * <ul>
 *   <li><strong>Async Processing:</strong> Habilita processamento assíncrono quando necessário</li>
 *   <li><strong>Thread Monitoring:</strong> Loga informações sobre Virtual Threads na inicialização</li>
 *   <li><strong>Performance Tuning:</strong> Configurações específicas para alta concorrência</li>
 * </ul>
 * 
 * <h3>Por que esta configuração?</h3>
 * <p>Virtual Threads são gerenciadas pela JVM, mas ainda precisamos garantir
 * que o sistema está configurado corretamente para aproveitar ao máximo
 * o potencial de alta concorrência.</p>
 * 
 * @author Marcelo
 */
@Slf4j
@Configuration
@EnableAsync
public class PerformanceConfig {
    
    /**
     * Loga informações sobre Virtual Threads na inicialização.
     * 
     * <p>Útil para verificar se Virtual Threads estão habilitadas
     * e monitorar o estado inicial do sistema.</p>
     */
    public PerformanceConfig() {
        ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
        
        // Verificar versão do Java (Java 21+ suporta Virtual Threads)
        String javaVersion = System.getProperty("java.version");
        boolean isJava21Plus = javaVersion != null && 
            (javaVersion.startsWith("21") || 
             javaVersion.startsWith("22") || 
             javaVersion.startsWith("23") ||
             Integer.parseInt(javaVersion.split("\\.")[0]) >= 21);
        
        if (isJava21Plus) {
            log.info("✅ Java {} detected - Virtual Threads are SUPPORTED", javaVersion);
            log.info("📊 Initial thread count: {}", threadMX.getThreadCount());
        } else {
            log.warn("⚠️ Java {} detected - Virtual Threads require Java 21+. Upgrade recommended.", javaVersion);
        }
        
        // Verificar se estamos usando Virtual Threads
        String virtualThreadsEnabled = System.getProperty("spring.threads.virtual.enabled", "false");
        if ("true".equalsIgnoreCase(virtualThreadsEnabled)) {
            log.info("✅ Virtual Threads are ENABLED in Spring Boot configuration");
        } else {
            log.info("ℹ️ Virtual Threads configuration: {} (check application.yml)", virtualThreadsEnabled);
        }
    }
}

