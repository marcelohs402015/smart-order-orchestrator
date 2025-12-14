package com.marcelo.orchestrator.presentation.mapper;

import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.presentation.dto.OrderItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para conversão entre DTOs de apresentação e objetos de domínio.
 * 
 * <p>Converte DTOs de request/response para objetos de domínio e vice-versa.
 * Mantém separação entre camada de apresentação e domínio.</p>
 * 
 * <h3>Arquitetura Hexagonal - Injeção de Dependência Explícita:</h3>
 * <ul>
 *   <li><strong>Component Spring:</strong> Gerenciado pelo Spring Container via @Component</li>
 *   <li><strong>Inversão de Controle:</strong> Injetado via construtor (SOLID - Dependency Inversion)</li>
 *   <li><strong>Controle Explícito:</strong> Código de mapeamento visível e manutenível</li>
 *   <li><strong>Testabilidade:</strong> Fácil de mockar e testar</li>
 * </ul>
 * 
 * <h3>Por que não usar MapStruct?</h3>
 * <ul>
 *   <li><strong>Controle Explícito:</strong> Código visível no projeto, não gerado</li>
 *   <li><strong>Consistência:</strong> Mesma abordagem do SagaExecutionRepositoryAdapter</li>
 *   <li><strong>Flexibilidade:</strong> Fácil adicionar validações, logs, transformações</li>
 *   <li><strong>Debugging:</strong> Stack traces apontam para código real</li>
 *   <li><strong>Simplicidade:</strong> Sem dependência de annotation processing</li>
 * </ul>
 * 
 * <h3>Validação:</h3>
 * <p>Validações de entrada são feitas via Bean Validation (@Valid) no controller.
 * Este mapper assume que os DTOs já foram validados e apenas converte entre camadas.</p>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
public class OrderPresentationMapper {
    
    /**
     * Converte OrderItemRequest para OrderItem (domínio).
     * 
     * <p>Valida null e converte campos manualmente, garantindo controle total
     * sobre o processo de mapeamento.</p>
     * 
     * @param request DTO de request (não pode ser null)
     * @return Objeto de domínio OrderItem
     * @throws IllegalArgumentException se request for null
     */
    public OrderItem toDomain(OrderItemRequest request) {
        if (request == null) {
            log.warn("Attempted to map null OrderItemRequest to domain");
            throw new IllegalArgumentException("OrderItemRequest cannot be null");
        }
        
        log.debug("Mapping OrderItemRequest to domain: productId={}, productName={}", 
            request.getProductId(), request.getProductName());
        
        return OrderItem.builder()
            .productId(request.getProductId())
            .productName(request.getProductName())
            .quantity(request.getQuantity())
            .unitPrice(request.getUnitPrice())
            .build();
    }
    
    /**
     * Converte lista de OrderItemRequest para lista de OrderItem (domínio).
     * 
     * <p>Valida lista e converte cada elemento usando o método de elemento único.
     * Retorna lista vazia se input for null ou vazio.</p>
     * 
     * @param requests Lista de DTOs de request
     * @return Lista de objetos de domínio OrderItem
     */
    public List<OrderItem> toDomainList(List<OrderItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.debug("Mapping empty or null list of OrderItemRequest to domain");
            return List.of();
        }
        
        log.debug("Mapping {} OrderItemRequest(s) to domain", requests.size());
        
        return requests.stream()
            .map(this::toDomain)
            .toList(); // Java 16+ - mais conciso que Collectors.toList()
    }
}

