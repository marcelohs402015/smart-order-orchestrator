package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Money;
import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.domain.model.OrderNumber;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    /**
     * Instância do mapper (gerada por MapStruct).
     */
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    
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
        if (order.getItems() != null) {
            entity.setItems(mapItemsToEntity(order.getItems(), entity));
        }
    }
    
    /**
     * Converte OrderEntity (JPA) para Order (domínio).
     * 
     * @param entity Entidade JPA
     * @return Entidade de domínio
     */
    @Mapping(target = "items", expression = "java(mapItemsToDomain(entity.getItems()))")
    Order toDomain(OrderEntity entity);
    
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
     * @param items Lista de itens do domínio
     * @param order Pedido pai (para relacionamento)
     * @return Lista de entidades JPA
     */
    default List<OrderItemEntity> mapItemsToEntity(List<OrderItem> items, OrderEntity order) {
        if (items == null) {
            return null;
        }
        return items.stream()
            .map(item -> OrderItemEntity.builder()
                .id(java.util.UUID.randomUUID()) // ID gerado automaticamente (JPA)
                .order(order)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
    
    default List<OrderItem> mapItemsToDomain(List<OrderItemEntity> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
            .map(item -> OrderItem.builder()
                // OrderItem do domínio não tem ID (é Value Object)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
    
}

