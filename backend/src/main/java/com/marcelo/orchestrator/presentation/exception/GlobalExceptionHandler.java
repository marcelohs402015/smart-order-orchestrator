package com.marcelo.orchestrator.presentation.exception;

import com.marcelo.orchestrator.application.exception.DomainException;
import com.marcelo.orchestrator.application.exception.InvalidOrderStatusException;
import com.marcelo.orchestrator.application.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler global para tratamento centralizado de exceções.
 * 
 * <p>Captura exceções lançadas em qualquer controller e retorna
 * respostas HTTP apropriadas com mensagens de erro estruturadas.</p>
 * 
 * <h3>Benefícios:</h3>
 * <ul>
 *   <li><strong>Centralização:</strong> Tratamento de erros em um único lugar</li>
 *   <li><strong>Consistência:</strong> Formato de erro padronizado</li>
 *   <li><strong>Segurança:</strong> Não expõe detalhes internos do sistema</li>
 *   <li><strong>UX:</strong> Mensagens de erro claras para o cliente</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Trata exceções de validação (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid request data",
            errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata exceções de domínio (OrderNotFoundException).
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Order Not Found",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Trata exceções de domínio (InvalidOrderStatusException).
     */
    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatusException(InvalidOrderStatusException ex) {
        log.warn("Invalid order status: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Order Status",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata exceções genéricas de domínio.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Domain Error",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata IllegalArgumentException (validações de aplicação).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Trata IllegalStateException (estados inválidos).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Invalid State",
            ex.getMessage(),
            null
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Trata exceções não mapeadas (fallback).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

