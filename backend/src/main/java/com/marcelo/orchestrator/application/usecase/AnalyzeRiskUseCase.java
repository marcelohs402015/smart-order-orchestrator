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

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeRiskUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final RiskAnalysisPort riskAnalysisPort;
    
    @Value("${features.riskAnalysis.enabled:true}")
    private boolean riskAnalysisEnabled;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order execute(AnalyzeRiskCommand command) {
        log.info("Analyzing risk for order: {}", command.getOrderId());
        
        
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        
        
        if (!riskAnalysisEnabled) {
            log.info("Risk analysis feature is DISABLED. Skipping OpenAI call for order {}. Current riskLevel: {}",
                order.getId(), order.getRiskLevel());
            
            return orderRepository.save(order);
        }
        
        
        if (!(order.isPaid() || order.getStatus() == OrderStatus.PAYMENT_PENDING)) {
            throw new IllegalStateException(
                String.format("Cannot analyze risk for order %s. Order must be PAID or PAYMENT_PENDING. Current status: %s",
                    order.getId(), order.getStatus())
            );
        }
        
        
        RiskAnalysisRequest request = new RiskAnalysisRequest(
            order.getId(),
            order.getTotalAmount(),
            order.getCustomerId(),
            order.getCustomerEmail(),
            command.getPaymentMethod(),
            buildAdditionalContext(order)
        );
        
        
        try {
            RiskAnalysisResult result = riskAnalysisPort.analyzeRisk(request);
            
            
            order = updateOrderRiskLevel(order, result);
            
            log.info("Risk analysis completed for order: {} - Risk Level: {} - Reason: {}",
                order.getId(), order.getRiskLevel(), result.reason());
        } catch (Exception e) {
            
            log.warn("Risk analysis failed for order: {} - Keeping risk level as PENDING. Error: {}",
                order.getId(), e.getMessage());
            
        }
        
        
        return orderRepository.save(order);
    }
    
    private Order updateOrderRiskLevel(Order order, RiskAnalysisResult result) {
        
        order.updateRiskLevel(result.riskLevel());
        return order;
    }
    
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

