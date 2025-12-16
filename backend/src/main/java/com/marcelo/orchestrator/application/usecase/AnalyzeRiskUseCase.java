package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisPort;
import com.marcelo.orchestrator.domain.port.RiskAnalysisRequest;
import com.marcelo.orchestrator.domain.port.RiskAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para análise de risco utilizando IA.
 * 
 * <p>Orquestra a análise de risco de um pedido utilizando serviços de IA
 * (OpenAI/MCP) para classificar o nível de risco (LOW, HIGH).</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Buscar pedido (deve estar PAID)</li>
 *   <li>Validar que pedido está em estado válido para análise</li>
 *   <li>Chamar serviço de IA através do RiskAnalysisPort</li>
 *   <li>Atualizar nível de risco do pedido</li>
 *   <li>Tratar falhas graciosamente (fallback para PENDING)</li>
 * </ul>
 * 
 * <h3>Resiliência:</h3>
 * <p>Se o serviço de IA estiver indisponível, o pedido mantém risco PENDING.
 * Isso permite que o sistema continue funcionando mesmo com IA offline.</p>
 * 
 * <h3>Fluxo:</h3>
 * <ol>
 *   <li>Valida que pedido existe e está PAID</li>
 *   <li>Cria RiskAnalysisRequest com metadados do pedido</li>
 *   <li>Chama RiskAnalysisPort (pode falhar - fallback gracioso)</li>
 *   <li>Atualiza riskLevel do pedido (LOW, HIGH, ou mantém PENDING se falhou)</li>
 *   <li>Persiste mudança</li>
 * </ol>
 * 
 * @author Marcelo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeRiskUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final RiskAnalysisPort riskAnalysisPort;
    
    /**
     * Feature flag para habilitar/desabilitar a análise de risco via IA.
     * 
     * <p>Permite desligar a integração com OpenAI sem alterar código, apenas por configuração.
     * Default: true.</p>
     */
    @Value("${features.riskAnalysis.enabled:true}")
    private boolean riskAnalysisEnabled;
    
    /**
     * Analisa risco de um pedido utilizando IA.
     * 
     * <p><strong>Padrão Saga:</strong> Usa REQUIRES_NEW para garantir transação
     * independente que faz commit imediato, permitindo compensação manual se
     * passos subsequentes falharem.</p>
     * 
     * @param command Command com ID do pedido
     * @return Pedido atualizado com nível de risco
     * @throws OrderNotFoundException se pedido não encontrado
     * @throws IllegalStateException se estado inválido para análise de risco
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order execute(AnalyzeRiskCommand command) {
        log.info("Analyzing risk for order: {}", command.getOrderId());
        
        // Buscar pedido
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        // Feature flag: permite desabilitar a análise de risco via configuração
        if (!riskAnalysisEnabled) {
            log.info("Risk analysis feature is DISABLED. Skipping OpenAI call for order {}. Current riskLevel: {}",
                order.getId(), order.getRiskLevel());
            // Mantém fluxo de saga, apenas não chama IA. Persiste estado atual para consistência.
            return orderRepository.save(order);
        }
        
        // Validar estado - analisa pedidos PAID ou PAYMENT_PENDING (pagamento em andamento)
        if (!(order.isPaid() || order.getStatus() == OrderStatus.PAYMENT_PENDING)) {
            throw new IllegalStateException(
                String.format("Cannot analyze risk for order %s. Order must be PAID or PAYMENT_PENDING. Current status: %s",
                    order.getId(), order.getStatus())
            );
        }
        
        // Criar requisição de análise
        RiskAnalysisRequest request = new RiskAnalysisRequest(
            order.getId(),
            order.getTotalAmount(),
            order.getCustomerId(),
            order.getCustomerEmail(),
            command.getPaymentMethod(),
            buildAdditionalContext(order)
        );
        
        // Analisar risco (pode falhar - fallback gracioso)
        try {
            RiskAnalysisResult result = riskAnalysisPort.analyzeRisk(request);
            
            // Atualizar nível de risco
            order = updateOrderRiskLevel(order, result);
            
            log.info("Risk analysis completed for order: {} - Risk Level: {} - Reason: {}",
                order.getId(), order.getRiskLevel(), result.reason());
        } catch (Exception e) {
            // Fallback gracioso: mantém PENDING se análise falhar
            log.warn("Risk analysis failed for order: {} - Keeping risk level as PENDING. Error: {}",
                order.getId(), e.getMessage());
            // Risk level permanece PENDING (já é o valor inicial)
        }
        
        // Persistir (mesmo se análise falhou, pode ter outras mudanças)
        return orderRepository.save(order);
    }
    
    /**
     * Atualiza o nível de risco do pedido baseado no resultado da análise.
     * 
     * @param order Pedido a atualizar
     * @param result Resultado da análise de risco
     * @return Pedido atualizado
     */
    private Order updateOrderRiskLevel(Order order, RiskAnalysisResult result) {
        // Atualiza riskLevel usando método do domínio
        order.updateRiskLevel(result.riskLevel());
        return order;
    }
    
    /**
     * Constrói contexto adicional para análise de risco.
     * 
     * <p>Agrega informações relevantes do pedido em formato texto
     * para envio ao serviço de IA (análise semântica).</p>
     * 
     * @param order Pedido a analisar
     * @return String com contexto formatado
     */
    private String buildAdditionalContext(Order order) {
        return String.format(
            "Order: %s, Amount: %s, Customer: %s, Items: %d",
            order.getOrderNumber(),
            order.getTotalAmount(),
            order.getCustomerEmail(),
            order.getItems() != null ? order.getItems().size() : 0
        );
    }
}

