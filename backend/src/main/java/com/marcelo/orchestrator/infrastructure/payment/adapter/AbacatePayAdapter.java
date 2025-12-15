package com.marcelo.orchestrator.infrastructure.payment.adapter;

import com.marcelo.orchestrator.domain.port.PaymentGatewayPort;
import com.marcelo.orchestrator.domain.port.PaymentRequest;
import com.marcelo.orchestrator.domain.port.PaymentResult;
import com.marcelo.orchestrator.domain.port.PaymentStatus;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayCustomerRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayProductRequest;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingResponse;
import com.marcelo.orchestrator.infrastructure.payment.dto.AbacatePayBillingListResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class AbacatePayAdapter implements PaymentGatewayPort {
    
    private final WebClient abacatePayWebClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    
    /**
     * Construtor com injeção de dependências.
     * 
     * <p>Padrão: Constructor Injection - campos final garantem imutabilidade
     * e seguem boas práticas do Spring.</p>
     */
    public AbacatePayAdapter(
            WebClient abacatePayWebClient,
            @Value("${abacatepay.api.base-url:https://api.abacatepay.com/v1}") String baseUrl,
            ObjectMapper objectMapper) {
        this.abacatePayWebClient = abacatePayWebClient;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }
    
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
        log.info("Processing payment via AbacatePay for order: {} - amount: {} {}, method: {}",
            request.orderId(), request.amount(), request.currency(), request.paymentMethod());
        
        try {
            // Converter PaymentRequest (domínio) para AbacatePayBillingRequest (API)
            AbacatePayBillingRequest billingRequest = buildBillingRequest(request);
            
            // Log do JSON que será enviado para o AbacatePay em um bloco bem visível
            try {
                String requestJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(billingRequest);
                log.info("--------------------------- ABACATEPAY INICIO JSON -------------------------------------------");
                log.info("OrderId={}", request.orderId());
                log.info("{}", requestJson);
                log.info("--------------------------- ABACATEPAY JSON FIM -------------------------------------------");
            } catch (Exception e) {
                log.warn("[AbacatePay] Failed to serialize request JSON for order {}: {}", 
                    request.orderId(), e.getMessage());
            }
            
            // Chamar API do AbacatePay
            AbacatePayBillingResponse response = abacatePayWebClient
                .post()
                .uri("/billing/create")
                .bodyValue(billingRequest)
                .retrieve()
                .bodyToMono(AbacatePayBillingResponse.class)
                .block(); // Bloqueia para retorno síncrono (pode usar reativo no futuro)
            
            // Converter resposta para PaymentResult (domínio)
            PaymentResult result = mapToPaymentResult(response, request.amount(), request.orderId());
            log.info("[AbacatePay] Billing created | orderId={} | paymentId={} | status={} | amount={} | devMode={}",
                request.orderId(),
                result.paymentId(),
                result.status(),
                result.amount(),
                response != null && response.data() != null ? response.data().devMode() : null);
            return result;
            
        } catch (WebClientResponseException e) {
            log.error("[AbacatePay] API error for order {}: status={} body={}",
                request.orderId(), e.getStatusCode(), e.getResponseBodyAsString());
            return createFailedResult(
                request.amount(),
                String.format("AbacatePay API error: %s", e.getStatusCode())
            );
        } catch (Exception e) {
            log.error("[AbacatePay] Unexpected error processing payment for order {}: {}", 
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
            // AbacatePay não tem endpoint direto para buscar billing por ID
            // Precisamos listar todas as cobranças e filtrar pelo paymentId
            AbacatePayBillingListResponse listResponse = abacatePayWebClient
                .get()
                .uri("/billing/list")
                .retrieve()
                .bodyToMono(AbacatePayBillingListResponse.class)
                .block();
            
            if (listResponse == null || !listResponse.isSuccess() || listResponse.data() == null) {
                log.warn("Empty or error response when listing billings. Payment ID: {}", paymentId);
                return PaymentStatus.PENDING;
            }
            
            log.info("Listed {} billings from AbacatePay. Searching for paymentId: {}", 
                listResponse.data().size(), paymentId);
            
            // Filtrar a lista para encontrar a cobrança com o paymentId desejado
            AbacatePayBillingResponse.AbacatePayBillingData billing = listResponse.data().stream()
                .filter(b -> paymentId.equals(b.id()))
                .findFirst()
                .orElse(null);
            
            if (billing == null) {
                log.warn("Billing not found in list. Payment ID: {}", paymentId);
                return PaymentStatus.PENDING;
            }
            
            String status = billing.status();
            PaymentStatus mappedStatus = mapAbacatePayStatus(status);
            log.info("Payment status from AbacatePay. Payment ID: {}, status: {}, mappedStatus: {}",
                paymentId, status, mappedStatus);
            return mappedStatus;
        } catch (WebClientResponseException e) {
            log.error("[AbacatePay] API error checking payment status | paymentId={} | httpStatus={} | body={} | URL={}",
                paymentId, e.getStatusCode(), e.getResponseBodyAsString(), 
                e.getRequest() != null ? e.getRequest().getURI() : "unknown");
            return PaymentStatus.PENDING; // Retorna PENDING em caso de erro
        } catch (Exception e) {
            log.error("Error checking payment status for paymentId={}: {}", paymentId, e.getMessage(), e);
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
        // Mapear produtos de forma genérica, já que PaymentRequest não possui itens.
        AbacatePayProductRequest product = AbacatePayProductRequest.builder()
            .externalId(request.orderId().toString())
            .name(String.format("Order %s", request.orderId()))
            .description("Smart Order Orchestrator billing")
            .quantity(1)
            .price(AbacatePayBillingRequest.toCents(request.amount()))
            .build();
        
        // Cliente mockado a partir do e-mail disponível
        AbacatePayCustomerRequest customer = AbacatePayCustomerRequest.builder()
            .name("Smart Order Customer")
            .cellphone("(11) 4002-8922")
            .email(request.customerEmail())
            // Tax ID mocked with a valid CPF used only for sandbox/testing.
            .taxId("810.373.590-62")
            .build();
        
        return AbacatePayBillingRequest.builder()
            // Método de pagamento: PIX (único método suportado pela API do AbacatePay)
            .methods(new String[]{"PIX"})
            .frequency("ONE_TIME")
            .products(new AbacatePayProductRequest[]{product})
            .returnUrl("https://example.com/billing")
            .completionUrl("https://example.com/completion")
            .customer(customer)
            .build();
    }
    
    /**
     * Converte resposta do AbacatePay para PaymentResult do domínio.
     * 
     * <p>Identifica ambiente de teste através do campo {@code devMode} retornado pela API.</p>
     */
    private PaymentResult mapToPaymentResult(AbacatePayBillingResponse response, BigDecimal originalAmount, java.util.UUID orderId) {
        if (response == null || !response.isSuccess() || response.data() == null) {
            String errorMessage = response != null && response.error() != null
                ? response.error()
                : "Unknown error from AbacatePay";
            log.warn("[AbacatePay] Billing failed or empty response | orderId={} | error={}",
                orderId, errorMessage);
            return createFailedResult(
                originalAmount,
                errorMessage
            );
        }
        
        AbacatePayBillingResponse.AbacatePayBillingData data = response.data();
        
        // Identificar ambiente de teste através do devMode
        if (Boolean.TRUE.equals(data.devMode())) {
            log.info("🧪 [AbacatePay][DEV MODE] Billing in TEST environment. Payment ID: {} - status: {}", 
                data.id(), data.status());
        } else {
            log.info("✅ [AbacatePay][PRODUCTION] Billing in PRODUCTION environment. Payment ID: {} - status: {}", 
                data.id(), data.status());
        }
        
        // Mapear status do AbacatePay para PaymentStatus do domínio
        PaymentStatus status = mapAbacatePayStatus(data.status());
        
        return new PaymentResult(
            data.id(), // ID da cobrança no AbacatePay
            status,
            status == PaymentStatus.SUCCESS ? "Payment processed successfully" : "Payment pending",
            BigDecimal.valueOf(data.amount()).divide(BigDecimal.valueOf(100)), // Converter centavos para reais
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

