package com.marcelo.orchestrator.infrastructure.payment.adapter;

import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.infrastructure.aspect.Loggable;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayCustomerRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayProductRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingResponse;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingListResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Loggable
@Component
public class AbacatePayAdapter implements PaymentGatewayPort {
    
    private final WebClient abacatePayWebClient;
    private final Executor virtualThreadExecutor;
    
    public AbacatePayAdapter(
            WebClient abacatePayWebClient,
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.abacatePayWebClient = abacatePayWebClient;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }
    
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentGateway")
    public PaymentResult processPayment(PaymentRequest request) {
        AbacatePayBillingRequest billingRequest = buildBillingRequest(request);
        
        CompletableFuture<AbacatePayBillingResponse> future = CompletableFuture.supplyAsync(() ->
            abacatePayWebClient
                .post()
                .uri("/billing/create")
                .bodyValue(billingRequest)
                .retrieve()
                .bodyToMono(AbacatePayBillingResponse.class)
                .block(), virtualThreadExecutor);
        
        try {
            AbacatePayBillingResponse response = future.join();
            return mapToPaymentResult(response, request.amount(), request.orderId());
        } catch (Exception e) {
            return createFailedResult(request.amount(), "Error: " + e.getMessage());
        }
    }
    
    private PaymentResult processPaymentFallback(PaymentRequest request, Exception e) {
        return createFailedResult(
            request.amount(),
            "Payment gateway temporarily unavailable (circuit breaker open)"
        );
    }
    
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "refundPaymentFallback")
    @Retry(name = "paymentGateway")
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        return createFailedResult(amount, "Refund not yet implemented");
    }
    
    private PaymentResult refundPaymentFallback(String paymentId, BigDecimal amount, Exception e) {
        return createFailedResult(amount, "Refund gateway temporarily unavailable");
    }
    
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "checkPaymentStatusFallback")
    public PaymentStatus checkPaymentStatus(String paymentId) {
        CompletableFuture<AbacatePayBillingListResponse> future = CompletableFuture.supplyAsync(() ->
            abacatePayWebClient
                .get()
                .uri("/billing/list")
                .retrieve()
                .bodyToMono(AbacatePayBillingListResponse.class)
                .block(), virtualThreadExecutor);
        
        try {
            AbacatePayBillingListResponse listResponse = future.join();
            
            if (listResponse == null || !listResponse.isSuccess() || listResponse.data() == null) {
                return PaymentStatus.PENDING;
            }
            
            AbacatePayBillingResponse.AbacatePayBillingData billing = listResponse.data().stream()
                .filter(b -> paymentId.equals(b.id()))
                .findFirst()
                .orElse(null);
            
            if (billing == null) {
                return PaymentStatus.PENDING;
            }
            
            return mapAbacatePayStatus(billing.status());
        } catch (Exception e) {
            return PaymentStatus.PENDING;
        }
    }
    
    private PaymentStatus checkPaymentStatusFallback(String paymentId, Exception e) {
        return PaymentStatus.PENDING;
    }
    
    private AbacatePayBillingRequest buildBillingRequest(PaymentRequest request) {
        AbacatePayProductRequest product = AbacatePayProductRequest.builder()
            .externalId(request.orderId().toString())
            .name(String.format("Order %s", request.orderId()))
            .description("Smart Order Orchestrator billing")
            .quantity(1)
            .price(AbacatePayBillingRequest.toCents(request.amount()))
            .build();
        
        AbacatePayCustomerRequest customer = AbacatePayCustomerRequest.builder()
            .name("Smart Order Customer")
            .cellphone("(11) 4002-8922")
            .email(request.customerEmail())
            .taxId("810.373.590-62")
            .build();
        
        return AbacatePayBillingRequest.builder()
            .methods(new String[]{"PIX"})
            .frequency("ONE_TIME")
            .products(new AbacatePayProductRequest[]{product})
            .returnUrl("https://example.com/billing")
            .completionUrl("https://example.com/completion")
            .customer(customer)
            .build();
    }
    
    private PaymentResult mapToPaymentResult(AbacatePayBillingResponse response, BigDecimal originalAmount, java.util.UUID orderId) {
        if (response == null || !response.isSuccess() || response.data() == null) {
            String errorMessage = response != null && response.error() != null
                ? response.error()
                : "Unknown error from AbacatePay";
            return createFailedResult(originalAmount, errorMessage);
        }
        
        AbacatePayBillingResponse.AbacatePayBillingData data = response.data();
        PaymentStatus status = mapAbacatePayStatus(data.status());
        
        return new PaymentResult(
            data.id(),
            status,
            status == PaymentStatus.SUCCESS ? "Payment processed successfully" : "Payment pending",
            BigDecimal.valueOf(data.amount()).divide(BigDecimal.valueOf(100)),
            LocalDateTime.now()
        );
    }
    
    private PaymentStatus mapAbacatePayStatus(String abacatePayStatus) {
        if (abacatePayStatus == null) {
            return PaymentStatus.PENDING;
        }
        
        return switch (abacatePayStatus.toUpperCase()) {
            case "PAID" -> PaymentStatus.SUCCESS;
            case "FAILED", "CANCELLED" -> PaymentStatus.FAILED;
            case "PENDING" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
    
    private PaymentResult createFailedResult(BigDecimal amount, String message) {
        return new PaymentResult(
            null,
            PaymentStatus.FAILED,
            message,
            amount,
            LocalDateTime.now()
        );
    }
}

