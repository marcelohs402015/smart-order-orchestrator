package com.marcelo.orchestrator.infrastructure.persistence.adapter;

import com.marcelo.orchestrator.AbstractIntegrationTest;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.model.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DisplayName("OrderRepositoryAdapter Integration Tests")
class OrderRepositoryAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepositoryAdapter orderRepositoryAdapter;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .status(OrderStatus.PENDING)
                .customerId(UUID.randomUUID())
                .customerName("Test Customer")
                .customerEmail("test@customer.com")
                .items(List.of(
                        OrderItem.builder()
                                .productId(UUID.randomUUID())
                                .productName("Product 1")
                                .quantity(2)
                                .unitPrice(BigDecimal.valueOf(50.00))
                                .build(),
                        OrderItem.builder()
                                .productId(UUID.randomUUID())
                                .productName("Product 2")
                                .quantity(1)
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .build()
                ))
                .totalAmount(BigDecimal.valueOf(200.00))
                .riskLevel(RiskLevel.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save new order with items to PostgreSQL")
    void shouldSaveNewOrder() {
        Order savedOrder = orderRepositoryAdapter.save(testOrder);

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(testOrder.getId());
        assertThat(savedOrder.getOrderNumber()).isEqualTo(testOrder.getOrderNumber());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
    }

    @Test
    @DisplayName("Should update existing order")
    void shouldUpdateExistingOrder() {
        Order savedOrder = orderRepositoryAdapter.save(testOrder);

        savedOrder.updateStatus(OrderStatus.PAYMENT_PENDING);
        savedOrder.attachPaymentId("PAY-123456");

        Order updatedOrder = orderRepositoryAdapter.save(savedOrder);

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(updatedOrder.getPaymentId()).isEqualTo("PAY-123456");
    }

    @Test
    @DisplayName("Should find order by ID with items")
    void shouldFindOrderByIdWithItems() {
        orderRepositoryAdapter.save(testOrder);

        Optional<Order> foundOrder = orderRepositoryAdapter.findById(testOrder.getId());

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(testOrder.getId());
        assertThat(foundOrder.get().getItems()).hasSize(2);
        assertThat(foundOrder.get().getItems().get(0).getProductName()).isEqualTo("Product 1");
    }

    @Test
    @DisplayName("Should return empty when order not found by ID")
    void shouldReturnEmptyWhenOrderNotFound() {
        Optional<Order> foundOrder = orderRepositoryAdapter.findById(UUID.randomUUID());

        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("Should find order by order number")
    void shouldFindOrderByOrderNumber() {
        orderRepositoryAdapter.save(testOrder);

        Optional<Order> foundOrder = orderRepositoryAdapter.findByOrderNumber(testOrder.getOrderNumber());

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderNumber()).isEqualTo(testOrder.getOrderNumber());
    }

    @Test
    @DisplayName("Should find order by payment ID")
    void shouldFindOrderByPaymentId() {
        testOrder.attachPaymentId("PAY-789");
        orderRepositoryAdapter.save(testOrder);

        Optional<Order> foundOrder = orderRepositoryAdapter.findByPaymentId("PAY-789");

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getPaymentId()).isEqualTo("PAY-789");
    }

    @Test
    @DisplayName("Should find all orders")
    void shouldFindAllOrders() {
        orderRepositoryAdapter.save(testOrder);

        Order anotherOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .status(OrderStatus.PAID)
                .customerId(UUID.randomUUID())
                .customerName("Another Customer")
                .customerEmail("another@customer.com")
                .items(List.of(
                        OrderItem.builder()
                                .productId(UUID.randomUUID())
                                .productName("Product 3")
                                .quantity(1)
                                .unitPrice(BigDecimal.valueOf(75.00))
                                .build()
                ))
                .totalAmount(BigDecimal.valueOf(75.00))
                .riskLevel(RiskLevel.LOW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepositoryAdapter.save(anotherOrder);

        List<Order> allOrders = orderRepositoryAdapter.findAll();

        assertThat(allOrders).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allOrders).extracting(Order::getOrderNumber)
                .contains(testOrder.getOrderNumber(), anotherOrder.getOrderNumber());
    }

    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        orderRepositoryAdapter.save(testOrder);

        Order paidOrder = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .status(OrderStatus.PAID)
                .customerId(UUID.randomUUID())
                .customerName("Paid Customer")
                .customerEmail("paid@customer.com")
                .items(List.of(
                        OrderItem.builder()
                                .productId(UUID.randomUUID())
                                .productName("Product 4")
                                .quantity(1)
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .build()
                ))
                .totalAmount(BigDecimal.valueOf(100.00))
                .riskLevel(RiskLevel.LOW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepositoryAdapter.save(paidOrder);

        List<Order> pendingOrders = orderRepositoryAdapter.findByStatus(OrderStatus.PENDING);
        List<Order> paidOrders = orderRepositoryAdapter.findByStatus(OrderStatus.PAID);

        assertThat(pendingOrders).hasSizeGreaterThanOrEqualTo(1);
        assertThat(paidOrders).hasSizeGreaterThanOrEqualTo(1);
        assertThat(pendingOrders).extracting(Order::getStatus).containsOnly(OrderStatus.PENDING);
        assertThat(paidOrders).extracting(Order::getStatus).containsOnly(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should delete order by ID")
    void shouldDeleteOrderById() {
        orderRepositoryAdapter.save(testOrder);
        assertThat(orderRepositoryAdapter.existsById(testOrder.getId())).isTrue();

        orderRepositoryAdapter.deleteById(testOrder.getId());

        assertThat(orderRepositoryAdapter.existsById(testOrder.getId())).isFalse();
    }

    @Test
    @DisplayName("Should check if order exists by ID")
    void shouldCheckIfOrderExists() {
        assertThat(orderRepositoryAdapter.existsById(testOrder.getId())).isFalse();

        orderRepositoryAdapter.save(testOrder);

        assertThat(orderRepositoryAdapter.existsById(testOrder.getId())).isTrue();
    }

    @Test
    @DisplayName("Should preserve items after update")
    void shouldPreserveItemsAfterUpdate() {
        Order savedOrder = orderRepositoryAdapter.save(testOrder);
        assertThat(savedOrder.getItems()).hasSize(2);

        savedOrder.updateStatus(OrderStatus.PAYMENT_PENDING);
        Order updatedOrder = orderRepositoryAdapter.save(savedOrder);

        assertThat(updatedOrder.getItems()).hasSize(2);
        assertThat(updatedOrder.getItems().get(0).getProductName()).isEqualTo("Product 1");
        assertThat(updatedOrder.getItems().get(1).getProductName()).isEqualTo("Product 2");
    }
}
