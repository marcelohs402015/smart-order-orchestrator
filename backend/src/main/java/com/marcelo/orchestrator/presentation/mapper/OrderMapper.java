package com.marcelo.orchestrator.presentation.mapper;

import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.presentation.dto.OrderItemRequest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper MapStruct para conversão entre DTOs de apresentação e objetos de domínio.
 * 
 * <p>Converte DTOs de request/response para objetos de domínio e vice-versa.
 * Mantém separação entre camada de apresentação e domínio.</p>
 * 
 * <h3>Por que MapStruct?</h3>
 * <ul>
 *   <li><strong>Performance:</strong> Código gerado é otimizado (sem reflection)</li>
 *   <li><strong>Type Safety:</strong> Erros de mapeamento são detectados em compilação</li>
 *   <li><strong>Menos Boilerplate:</strong> Não precisa escrever mapeamento manual</li>
 *   <li><strong>Manutenibilidade:</strong> Mudanças no domínio são detectadas automaticamente</li>
 *   <li><strong>Consistência:</strong> Mesma abordagem usada na camada de infraestrutura</li>
 * </ul>
 * 
 * <h3>SOLID - Dependency Inversion:</h3>
 * <p>Usando {@code componentModel = "spring"}, o MapStruct gera um componente Spring
 * que pode ser injetado via construtor, seguindo o princípio de Dependency Inversion.
 * Isso torna o código testável e alinhado com as práticas do Spring Boot.</p>
 * 
 * <h3>Validação:</h3>
 * <p>Validações de entrada são feitas via Bean Validation (@Valid) no controller.
 * Este mapper assume que os DTOs já foram validados e apenas converte entre camadas.
 * O MapStruct gera validação de null automaticamente (fail-fast).</p>
 * 
 * <h3>Java 17+ Features:</h3>
 * <p>Utiliza {@code List.of()} e {@code stream().toList()} (Java 16+) para código mais conciso
 * e alinhado com as melhores práticas modernas do Java.</p>
 * 
 * @author Marcelo
 */
@Mapper(componentModel = "spring", implementationName = "OrderPresentationMapperImpl")
public interface OrderMapper {
    
    /**
     * Converte OrderItemRequest para OrderItem (domínio).
     * 
     * <p>MapStruct gera automaticamente o código de mapeamento em tempo de compilação.
     * Os campos com mesmo nome são mapeados automaticamente (productId, productName, quantity, unitPrice).
     * O código gerado valida null e lança exceção apropriada (fail-fast).</p>
     * 
     * @param request DTO de request (não pode ser null)
     * @return Objeto de domínio OrderItem
     */
    OrderItem toDomain(OrderItemRequest request);
    
    /**
     * Converte lista de OrderItemRequest para lista de OrderItem (domínio).
     * 
     * <p>MapStruct gera automaticamente este método baseado no método {@code toDomain}.
     * Cada elemento da lista é convertido usando o método de elemento único.</p>
     * 
     * @param requests Lista de DTOs de request
     * @return Lista de objetos de domínio OrderItem
     */
    List<OrderItem> toDomainList(List<OrderItemRequest> requests);
}

