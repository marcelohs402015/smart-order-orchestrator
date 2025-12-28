package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private com.marcelo.orchestrator.domain.model.OrderStatus status;
    
    
    @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
    private UUID customerId;
    
    
    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;
    
    
    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;
    
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();
    
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    
    @Column(name = "payment_id", length = 100)
    private String paymentId;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private com.marcelo.orchestrator.domain.model.RiskLevel riskLevel;
    
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    
    @Version
    @Column(name = "version")
    private Long version;
}

