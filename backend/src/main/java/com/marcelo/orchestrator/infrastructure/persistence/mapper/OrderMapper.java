package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper MapStruct para conversão entre Order (domínio) e OrderEntity (JPA).
 * 
 * <p>MapStruct gera código de mapeamento em tempo de compilação,
 * garantindo type-safety e performance (sem reflection em runtime).</p>
 * 
 * <h3>Por que MapStruct?</h3>
 * <ul>
 *   <li><strong>Performance:</strong> Código gerado é otimizado (sem reflection)</li>
 *   <li><strong>Type Safety:</strong> Erros de mapeamento são detectados em compilação</li>
 *   <li><strong>Menos Boilerplate:</strong> Não precisa escrever mapeamento manual</li>
 *   <li><strong>Manutenibilidade:</strong> Mudanças no domínio são detectadas automaticamente</li>
 * </ul>
 * 
 * <h3>Estratégia de Mapeamento:</h3>
 * <ul>
 *   <li><strong>@Mapping:</strong> Ignora campos que não devem ser mapeados (ex: version)</li>
 *   <li><strong>@MappingTarget:</strong> Para atualização de entidade existente</li>
 *   <li><strong>Métodos customizados:</strong> Para lógica de mapeamento complexa</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Mapper(componentModel = "spring", implementationName = "OrderPersistenceMapperImpl")
public interface OrderMapper {
    
    /**
     * Converte Order (domínio) para OrderEntity (JPA).
     * 
     * @param order Entidade de domínio
     * @return Entidade JPA
     */
    @Mapping(target = "version", ignore = true) // Version é gerenciado pelo JPA
    @Mapping(target = "items", ignore = true) // Itens são mapeados após criação da entidade
    OrderEntity toEntity(Order order);
    
    /**
     * Mapeia os itens após a criação da OrderEntity.
     * Necessário porque os itens precisam referenciar a OrderEntity pai.
     */
    @AfterMapping
    default void mapItemsAfterMapping(Order order, @MappingTarget OrderEntity entity) {
        // IMPORTANTE: Garantir que a lista está inicializada (já está com @Builder.Default, mas garantir)
        if (entity.getItems() == null) {
            entity.setItems(new java.util.ArrayList<>());
        }
        
        if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
            // IMPORTANTE: Não usar clear() + setItems() - isso quebra referência gerenciada pelo JPA
            // Se a lista já tem itens (não deveria na criação, mas garantir), limpar primeiro
            if (!entity.getItems().isEmpty()) {
                entity.getItems().clear();
            }
            
            // Adicionar novos itens na lista existente (mantém referência gerenciada pelo JPA)
            List<OrderItemEntity> newItems = mapItemsToEntity(order.getItems(), entity);
            entity.getItems().addAll(newItems);
        }
        // Se não há itens, a lista já está vazia (inicializada com @Builder.Default)
    }
    
    /**
     * Converte OrderEntity (JPA) para Order (domínio).
     * Como Order tem items como final, precisamos usar um método customizado completo.
     */
    default Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }
        
        List<OrderItem> items = entity.getItems() != null 
            ? mapItemsToDomain(entity.getItems())
            : List.of();
        
        return Order.builder()
            .id(entity.getId())
            .orderNumber(entity.getOrderNumber())
            .status(entity.getStatus())
            .customerId(entity.getCustomerId())
            .customerName(entity.getCustomerName())
            .customerEmail(entity.getCustomerEmail())
            .items(items)
            .totalAmount(entity.getTotalAmount())
            .paymentId(entity.getPaymentId())
            .riskLevel(entity.getRiskLevel())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    /**
     * Atualiza OrderEntity existente com dados de Order.
     * 
     * @param order Entidade de domínio com dados atualizados
     * @param entity Entidade JPA a ser atualizada
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "items", ignore = true) // Itens são atualizados separadamente
    void updateEntity(Order order, @MappingTarget OrderEntity entity);
    
    /**
     * Converte lista de OrderItem (domínio) para OrderItemEntity (JPA).
     * 
     * <p><strong>Importante:</strong> Não gera ID manualmente. O JPA gerencia via @GeneratedValue.
     * Isso evita erro "detached entity passed to persist" quando OrderEntity ainda não foi persistida.</p>
     * 
     * @param items Lista de itens do domínio
     * @param order Pedido pai (para relacionamento)
     * @return Lista de entidades JPA
     */
    default List<OrderItemEntity> mapItemsToEntity(List<OrderItem> items, OrderEntity order) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(item -> OrderItemEntity.builder()
                // ID não é definido aqui - será gerado pelo JPA via @GeneratedValue
                // Isso garante que a entidade seja tratada como "new" e não "detached"
                .order(order)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * Converte lista de OrderItemEntity (JPA) para OrderItem (domínio).
     * 
     * @param items Lista de entidades JPA
     * @return Lista de itens do domínio
     */
    default List<OrderItem> mapItemsToDomain(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(item -> OrderItem.builder()
                // OrderItem do domínio não tem ID (é Value Object)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(Collectors.toList());
    }
    
}
