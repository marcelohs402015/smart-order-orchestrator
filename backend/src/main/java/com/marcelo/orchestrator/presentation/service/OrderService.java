package com.marcelo.orchestrator.presentation.service;

import com.marcelo.orchestrator.application.saga.OrderSagaCommand;
import com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator;
import com.marcelo.orchestrator.application.saga.OrderSagaResult;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.presentation.dto.CreateOrderRequest;
import com.marcelo.orchestrator.presentation.dto.CreateOrderResponse;
import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import com.marcelo.orchestrator.presentation.mapper.OrderPresentationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderSagaOrchestrator sagaOrchestrator;
    private final OrderRepositoryPort orderRepository;
    private final OrderPresentationMapper orderMapper;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        
        String idempotencyKey = request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
            ? request.idempotencyKey()
            : generateIdempotencyKey(request);
        
        OrderSagaCommand command = OrderSagaCommand.builder()
            .idempotencyKey(idempotencyKey)
            .customerId(request.customerId())
            .customerName(request.customerName())
            .customerEmail(request.customerEmail())
            .items(orderMapper.toDomainList(request.items()))
            .paymentMethod(request.paymentMethod())
            .currency(request.currency() != null ? request.currency() : "BRL")
            .build();
        
        OrderSagaResult result = sagaOrchestrator.execute(command);
        
        return mapSagaResultToResponse(result);
    }

    public OrderResponse getOrderById(UUID id) {
        log.info("Finding order by ID: {}", id);
        return orderRepository.findById(id)
            .map(OrderResponse::from)
            .orElse(null);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        log.info("Finding order by number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
            .map(OrderResponse::from)
            .orElse(null);
    }

    public List<OrderResponse> getAllOrders(OrderStatus status) {
        List<OrderResponse> orders;
        
        if (status != null) {
            log.info("Finding orders by status: {}", status);
            orders = orderRepository.findByStatus(status).stream()
                .map(OrderResponse::from)
                .toList();
        } else {
            log.info("Finding all orders");
            orders = orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
        }
        
        return orders;
    }

    @Transactional
    public OrderResponse analyzeRiskManually(UUID orderId) {
        log.info("Manually triggering risk analysis for order {}", orderId);
        
        AnalyzeRiskCommand command = AnalyzeRiskCommand.builder()
            .orderId(orderId)
            .paymentMethod("PIX")
            .build();
        
        Order analyzedOrder = analyzeRiskUseCase.execute(command);
        return OrderResponse.from(analyzedOrder);
    }

    private CreateOrderResponse mapSagaResultToResponse(OrderSagaResult result) {
        if (result.isSuccess()) {
            return CreateOrderResponse.success(
                OrderResponse.from(result.getOrder()),
                result.getSagaExecutionId()
            );
        } else if (result.isInProgress()) {
            return CreateOrderResponse.inProgress(
                result.getOrder() != null ? OrderResponse.from(result.getOrder()) : null,
                result.getSagaExecutionId(),
                "Order creation is already in progress"
            );
        } else {
            return CreateOrderResponse.failed(
                result.getOrder() != null ? OrderResponse.from(result.getOrder()) : null,
                result.getSagaExecutionId(),
                result.getErrorMessage()
            );
        }
    }

    private String generateIdempotencyKey(CreateOrderRequest request) {
        try {
            StringBuilder data = new StringBuilder();
            data.append(request.customerId());
            data.append(request.customerEmail());
            data.append(request.paymentMethod());
            if (request.currency() != null) {
                data.append(request.currency());
            }
            
            if (request.items() != null) {
                request.items().forEach(item -> {
                    data.append(item.productId());
                    data.append(item.productName());
                    data.append(item.quantity());
                    data.append(item.unitPrice());
                });
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 not available, using UUID as fallback for idempotency key");
            return UUID.randomUUID().toString();
        }
    }
}