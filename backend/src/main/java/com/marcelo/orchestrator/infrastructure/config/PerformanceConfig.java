package com.marcelo.orchestrator.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Slf4j
@Configuration
@EnableAsync
public class PerformanceConfig {
    
    public PerformanceConfig() {
        ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
        
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
        
        String virtualThreadsEnabled = System.getProperty("spring.threads.virtual.enabled", "false");
        if ("true".equalsIgnoreCase(virtualThreadsEnabled)) {
            log.info("✅ Virtual Threads are ENABLED in Spring Boot configuration");
        } else {
            log.info("ℹ️ Virtual Threads configuration: {} (check application.yml)", virtualThreadsEnabled);
        }
    }
}
