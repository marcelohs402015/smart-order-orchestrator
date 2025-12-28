package com.marcelo.orchestrator.presentation.controller;

import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.application.usecase.RefreshPaymentStatusUseCase;
import com.marcelo.orchestrator.domain.event.saga.PaymentProcessedEvent;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import com.marcelo.orchestrator.domain.port.EventPublisherPort;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.presentation.dto.PaymentStatusResponse;
import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API for payment status operations")
public class PaymentController {

    private final PaymentGatewayPort paymentGatewayPort;
    private final RefreshPaymentStatusUseCase refreshPaymentStatusUseCase;
    private final OrderRepositoryPort orderRepository;
    private final EventPublisherPort eventPublisher;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;

    
    @GetMapping("/{paymentId}/status")
    @Operation(
        summary = "Check payment status",
        description = "Checks the current status of a payment in the external gateway (AbacatePay) using the paymentId (bill_xxx). " +
                      "Automatically updates the order in the database if the status has changed."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned successfully")
    })
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(
        @Parameter(description = "Payment ID in AbacatePay (e.g. bill_xxx)", required = true)
        @PathVariable String paymentId
    ) {
        log.info("Checking payment status via API for paymentId={}", paymentId);
        
        
        PaymentStatus gatewayStatus = paymentGatewayPort.checkPaymentStatus(paymentId);
        
        
        orderRepository.findByPaymentId(paymentId).ifPresent(order -> {
            PaymentStatus currentOrderStatus = mapOrderStatusToPaymentStatus(order.getStatus());
            
            if (currentOrderStatus != gatewayStatus) {
                log.info("Payment status changed for order {}. Current: {}, New: {}. Updating order.",
                    order.getId(), currentOrderStatus, gatewayStatus);
                
                
                boolean wasPaid = order.isPaid();
                
                updateOrderStatus(order, gatewayStatus);
                Order updatedOrder = orderRepository.save(order);
                
                
                if (!wasPaid && updatedOrder.isPaid()) {
                    publishPaymentProcessedEvent(updatedOrder);
                    
                    triggerRiskAnalysisIfNeeded(updatedOrder);
                }
                
                log.info("Order {} updated with new payment status: {}", updatedOrder.getId(), gatewayStatus);
            } else {
                log.debug("Payment status unchanged for order {}. Status: {}", order.getId(), gatewayStatus);
            }
        });
        
        PaymentStatusResponse response = new PaymentStatusResponse(paymentId, gatewayStatus);
        return ResponseEntity.ok(response);
    }
    
    
    private PaymentStatus mapOrderStatusToPaymentStatus(com.marcelo.orchestrator.domain.model.OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PAID -> PaymentStatus.SUCCESS;
            case PAYMENT_FAILED -> PaymentStatus.FAILED;
            case PAYMENT_PENDING -> PaymentStatus.PENDING;
            case CANCELED -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
    
    
    private void updateOrderStatus(Order order, PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case SUCCESS -> {
                if (!order.isPaid()) {
                    order.markAsPaid(order.getPaymentId());
                }
            }
            case FAILED, CANCELLED -> {
                if (!order.isPaymentFailed() && !order.isCanceled()) {
                    order.markAsPaymentFailed();
                }
            }
            case PENDING, REFUNDED -> {
                
                log.debug("Payment status is {} for order {}. Keeping current order status {}.",
                    paymentStatus, order.getId(), order.getStatus());
            }
        }
    }
    
    
    private void publishPaymentProcessedEvent(Order order) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.fromOrder(order);
            eventPublisher.publish(event);
            log.info("PaymentProcessedEvent published for order {} (status: PAID)", order.getId());
        } catch (Exception e) {
            
            log.error("Failed to publish PaymentProcessedEvent for order {}: {}", 
                order.getId(), e.getMessage(), e);
        }
    }

    
    @PostMapping("/orders/{orderId}/refresh-status")
    @Operation(
        summary = "Refresh payment status for order",
        description = "Refreshes the payment status of an order by querying the external gateway and updating the order accordingly."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order payment status refreshed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> refreshPaymentStatusForOrder(
        @Parameter(description = "Order ID", required = true)
        @PathVariable java.util.UUID orderId
    ) {
        log.info("Refreshing payment status for order {}", orderId);
        Order updatedOrder = refreshPaymentStatusUseCase.execute(orderId);
        
        
        triggerRiskAnalysisIfNeeded(updatedOrder);
        
        return ResponseEntity.ok(OrderResponse.from(updatedOrder));
    }
    
    
    private void triggerRiskAnalysisIfNeeded(Order order) {
        if (order == null) {
            return;
        }
        
        if (order.isPaid() && order.getRiskLevel() == RiskLevel.PENDING) {
            try {
                log.info("Triggering risk analysis after payment confirmation for order {}", order.getId());
                AnalyzeRiskCommand command = AnalyzeRiskCommand.builder()
                    .orderId(order.getId())
                    .paymentMethod("PIX") 
                    .build();
                analyzeRiskUseCase.execute(command);
            } catch (Exception e) {
                log.warn("Failed to analyze risk after payment confirmation for order {}: {}", 
                    order.getId(), e.getMessage());
            }
        } else {
            log.debug("Risk analysis not triggered for order {}. Status: {}, RiskLevel: {}", 
                order.getId(), order.getStatus(), order.getRiskLevel());
        }
    }
}


