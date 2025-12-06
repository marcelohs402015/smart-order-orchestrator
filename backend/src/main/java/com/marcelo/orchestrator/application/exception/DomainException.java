package com.marcelo.orchestrator.application.exception;

/**
 * Exceção base para erros de domínio.
 * 
 * <p>Representa erros que ocorrem na camada de domínio ou aplicação,
 * relacionados a regras de negócio violadas.</p>
 * 
 * <h3>Hierarquia de Exceções:</h3>
 * <ul>
 *   <li><strong>DomainException:</strong> Base para todas exceções de domínio</li>
 *   <li><strong>OrderNotFoundException:</strong> Pedido não encontrado</li>
 *   <li><strong>InvalidOrderStatusException:</strong> Status inválido ou transição não permitida</li>
 *   <li><strong>PaymentProcessingException:</strong> Erro ao processar pagamento</li>
 * </ul>
 * 
 * <h3>Por que Exceções de Domínio?</h3>
 * <ul>
 *   <li><strong>Type Safety:</strong> Diferencia erros de negócio de erros técnicos</li>
 *   <li><strong>Tratamento Específico:</strong> Camada Presentation pode tratar de forma diferente</li>
 *   <li><strong>Documentação:</strong> Exceções documentam casos de erro possíveis</li>
 * </ul>
 * 
 * @author Marcelo
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

