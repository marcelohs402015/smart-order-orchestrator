package com.marcelo.orchestrator.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Porta (Port) para integração com gateway de pagamento externo.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) para comunicação
 * com serviços externos de pagamento. A implementação na camada Infrastructure utilizará
 * Resilience4j (Circuit Breaker, Retry) para garantir resiliência.</p>
 * 
 * <h3>Arquitetura Hexagonal e Resiliência:</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na Infrastructure com HTTP client (WebClient/RestTemplate)</li>
 *   <li><strong>Circuit Breaker:</strong> Protege contra falhas em cascata quando gateway está indisponível</li>
 *   <li><strong>Retry:</strong> Tenta novamente em caso de falhas transitórias (timeout, 503, etc.)</li>
 * </ul>
 * 
 * <h3>Por que Interface ao invés de Classe Concreta?</h3>
 * <ul>
 *   <li><strong>Dependency Inversion Principle (SOLID):</strong> Domínio não depende de implementação</li>
 *   <li><strong>Testabilidade:</strong> Fácil mockar para testes unitários</li>
 *   <li><strong>Flexibilidade:</strong> Trocar gateway (Stripe, PayPal, AbacatePay) sem alterar domínio</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes HTTP, JSON, etc.</li>
 * </ul>
 * 
 * <h3>Resiliência em Integrações Externas:</h3>
 * <p>Integrações externas são pontos de falha comuns. Esta porta será implementada com:</p>
 * <ul>
 *   <li><strong>Circuit Breaker:</strong> Abre circuito após muitas falhas, evitando sobrecarga</li>
 *   <li><strong>Retry:</strong> Tenta novamente em falhas transitórias</li>
 *   <li><strong>Fallback:</strong> Estratégia alternativa quando gateway está indisponível</li>
 *   <li><strong>Timeout:</strong> Evita requisições travadas indefinidamente</li>
 * </ul>
 * 
 * @author Marcelo
 */
public interface PaymentGatewayPort {
    
    /**
     * Processa um pagamento no gateway externo.
     * 
     * <p>A implementação deve utilizar Circuit Breaker e Retry para garantir resiliência.
     * Em caso de falha, deve retornar {@link PaymentResult} com status de falha,
     * não lançar exceção (fail-fast pode ser configurado conforme necessidade).</p>
     * 
     * @param request Dados do pagamento a ser processado
     * @return Resultado do processamento (sucesso ou falha)
     */
    PaymentResult processPayment(PaymentRequest request);
    
    /**
     * Processa reembolso de um pagamento.
     * 
     * <p>Utilizado quando um pedido precisa ser cancelado após pagamento confirmado.
     * Também utiliza Circuit Breaker e Retry para resiliência.</p>
     * 
     * @param paymentId ID do pagamento original no gateway
     * @param amount Valor a ser reembolsado (pode ser parcial)
     * @return Resultado do reembolso (sucesso ou falha)
     */
    PaymentResult refundPayment(String paymentId, BigDecimal amount);
    
    /**
     * Verifica o status de um pagamento no gateway.
     * 
     * <p>Útil para sincronização e verificação de pagamentos pendentes.</p>
     * 
     * @param paymentId ID do pagamento no gateway
     * @return Status atual do pagamento
     */
    PaymentStatus checkPaymentStatus(String paymentId);
}

