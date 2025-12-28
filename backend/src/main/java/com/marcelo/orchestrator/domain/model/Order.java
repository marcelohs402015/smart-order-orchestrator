package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Order {
    
    private final UUID id;
    private final String orderNumber;
    private OrderStatus status;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final List<OrderItem> items;
    private BigDecimal totalAmount;
    private String paymentId;
    private RiskLevel riskLevel;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void calculateTotal() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        if (this.status == null) {
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now();
            return;
        }
        
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                    this.status, newStatus, this.status.getAllowedTransitions())
            );
        }
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsPaid(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be null or blank");
        }
        
        updateStatus(OrderStatus.PAID);
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsPaymentFailed() {
        updateStatus(OrderStatus.PAYMENT_FAILED);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }
    
    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }
    
    public void attachPaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPaymentPending() {
        return status == OrderStatus.PAYMENT_PENDING;
    }
    
    public boolean isCanceled() {
        return status == OrderStatus.CANCELED;
    }
    
    public boolean isPaymentFailed() {
        return status == OrderStatus.PAYMENT_FAILED;
    }
    
    public void updateRiskLevel(RiskLevel riskLevel) {
        if (riskLevel == null) {
            throw new IllegalArgumentException("Risk level cannot be null");
        }
        this.riskLevel = riskLevel;
        this.updatedAt = LocalDateTime.now();
    }
}
