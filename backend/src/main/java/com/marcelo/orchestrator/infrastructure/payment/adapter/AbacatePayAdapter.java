package com.marcelo.orchestrator.infrastructure.payment.adapter;

import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Adaptador que implementa PaymentGatewayPort usando AbacatePay.
 * 
 * <p>Este é o <strong>Adapter</strong> na Arquitetura Hexagonal.
 * Implementa a porta definida no domínio usando WebClient para comunicação HTTP
 * com a API do AbacatePay.</p>
 * 
 * <h3>Arquitetura Hexagonal:</h3>
 * <ul>
 *   <li><strong>Port:</strong> PaymentGatewayPort (definida no domínio)</li>
 *   <li><strong>Adapter:</strong> Esta classe (implementa a porta)</li>
 *   <li><strong>Inversão de Dependência:</strong> Domínio não conhece esta implementação</li>
 * </ul>
 * 
 * <h3>Resiliência com Resilience4j:</h3>
 * <ul>
 *   <li><strong>@CircuitBreaker:</strong> Protege contra falhas em cascata</li>
 *   <li><strong>@Retry:</strong> Tenta novamente em falhas transitórias</li>
 *   <li><strong>Fallback:</strong> Retorna PaymentResult com falha quando circuito aberto</li>
 * </ul>
 * 
 * <h3>WebClient (Reativo):</h3>
 * <p>Usa WebClient ao invés de RestTemplate porque:
 * - Suporte nativo a reatividade (compatível com Virtual Threads)
 * - Melhor performance em alta concorrência
 * - Suporte a Circuit Breaker reativo do Resilience4j</p>
 * 
 * <h3>Documentação AbacatePay:</h3>
 * <p>Base URL: https://api.abacatepay.com/v1</p>
 * <p>Autenticação: Bearer token no header Authorization</p>
 * <p>Endpoint: POST /billing/create</p>
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com">AbacatePay Documentation</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbacatePayAdapter implements PaymentGatewayPort {
    
    private final WebClient abacatePayWebClient;
    
    @Value("${abacatepay.api.base-url:https://api.abacatepay.com/v1}")
    private String baseUrl;
    
    /**
     * Processa pagamento no AbacatePay.
     * 
     * <p>Anotações do Resilience4j:
     * - @CircuitBreaker: Nome "paymentGateway" (configurado no application.properties)
     * - @Retry: Tenta novamente em falhas transitórias</p>
     * 
     * <p>Em caso de falha (circuito aberto, timeout, etc.),
     * retorna PaymentResult com status FAILED ao invés de lançar exceção.
     * Isso permite que o Use Case trate graciosamente.</p>
     */
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentGateway")
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing payment via AbacatePay for order: {}", request.orderId());
        
        try {
            // Converter PaymentRequest (domínio) para AbacatePayBillingRequest (API)
            AbacatePayBillingRequest billingRequest = buildBillingRequest(request);
            
            // Chamar API do AbacatePay
            AbacatePayBillingResponse response = abacatePayWebClient
                .post()
                .uri("/billing/create")
                .bodyValue(billingRequest)
                .retrieve()
                .bodyToMono(AbacatePayBillingResponse.class)
                .block(); // Bloqueia para retorno síncrono (pode usar reativo no futuro)
            
            // Converter resposta para PaymentResult (domínio)
            return mapToPaymentResult(response, request.amount());
            
        } catch (WebClientResponseException e) {
            log.error("AbacatePay API error for order {}: {} - {}", 
                request.orderId(), e.getStatusCode(), e.getResponseBodyAsString());
            return createFailedResult(
                request.amount(),
                String.format("AbacatePay API error: %s", e.getStatusCode())
            );
        } catch (Exception e) {
            log.error("Unexpected error processing payment for order {}: {}", 
                request.orderId(), e.getMessage(), e);
            return createFailedResult(request.amount(), "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Fallback method chamado quando Circuit Breaker está aberto.
     * 
     * <p>Retorna PaymentResult com falha ao invés de lançar exceção.
     * Isso permite degradação graciosa quando gateway está indisponível.</p>
     */
    private PaymentResult processPaymentFallback(PaymentRequest request, Exception e) {
        log.warn("Circuit breaker open for AbacatePay. Returning failed payment result. Order: {}", 
            request.orderId());
        return createFailedResult(
            request.amount(),
            "Payment gateway temporarily unavailable (circuit breaker open)"
        );
    }
    
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "refundPaymentFallback")
    @Retry(name = "paymentGateway")
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        log.info("Processing refund via AbacatePay for payment: {} - Amount: {}", paymentId, amount);
        
        // TODO: Implementar quando endpoint de reembolso estiver disponível na API
        // Por enquanto, retorna falha
        log.warn("Refund not yet implemented for AbacatePay. Payment ID: {}", paymentId);
        return createFailedResult(amount, "Refund not yet implemented");
    }
    
    /**
     * Fallback para reembolso.
     */
    private PaymentResult refundPaymentFallback(String paymentId, BigDecimal amount, Exception e) {
        log.warn("Circuit breaker open for refund. Payment ID: {}", paymentId);
        return createFailedResult(amount, "Refund gateway temporarily unavailable");
    }
    
    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "checkPaymentStatusFallback")
    public PaymentStatus checkPaymentStatus(String paymentId) {
        log.info("Checking payment status via AbacatePay: {}", paymentId);
        
        try {
            // TODO: Implementar quando endpoint de status estiver disponível
            // Por enquanto, retorna PENDING
            log.warn("Check payment status not yet implemented. Payment ID: {}", paymentId);
            return PaymentStatus.PENDING;
        } catch (Exception e) {
            log.error("Error checking payment status: {}", e.getMessage());
            return PaymentStatus.PENDING; // Retorna PENDING em caso de erro
        }
    }
    
    /**
     * Fallback para verificação de status.
     */
    private PaymentStatus checkPaymentStatusFallback(String paymentId, Exception e) {
        log.warn("Circuit breaker open for status check. Payment ID: {}", paymentId);
        return PaymentStatus.PENDING;
    }
    
    /**
     * Constrói AbacatePayBillingRequest a partir de PaymentRequest.
     * 
     * <p>Mapeia dados do domínio para formato esperado pela API do AbacatePay.</p>
     */
    private AbacatePayBillingRequest buildBillingRequest(PaymentRequest request) {
        return AbacatePayBillingRequest.builder()
            .amount(AbacatePayBillingRequest.toCents(request.amount()))
            .description(String.format("Pedido %s", request.orderId()))
            .methods(new String[]{"PIX", "CARD"}) // Métodos disponíveis
            .frequency("ONE_TIME") // Pagamento único
            // Cliente pode ser criado no momento da cobrança ou antes
            .build();
    }
    
    /**
     * Converte resposta do AbacatePay para PaymentResult do domínio.
     */
    private PaymentResult mapToPaymentResult(AbacatePayBillingResponse response, BigDecimal originalAmount) {
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return createFailedResult(
                originalAmount,
                response != null && response.getError() != null 
                    ? response.getError() 
                    : "Unknown error from AbacatePay"
            );
        }
        
        AbacatePayBillingResponse.AbacatePayBillingData data = response.getData();
        
        // Mapear status do AbacatePay para PaymentStatus do domínio
        PaymentStatus status = mapAbacatePayStatus(data.getStatus());
        
        return new PaymentResult(
            data.getId(), // ID da cobrança no AbacatePay
            status,
            status == PaymentStatus.SUCCESS ? "Payment processed successfully" : "Payment pending",
            BigDecimal.valueOf(data.getAmount()).divide(BigDecimal.valueOf(100)), // Converter centavos para reais
            LocalDateTime.now()
        );
    }
    
    /**
     * Mapeia status do AbacatePay para PaymentStatus do domínio.
     */
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
    
    /**
     * Cria PaymentResult com falha.
     */
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

