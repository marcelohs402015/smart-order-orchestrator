package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderItem;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Order (domínio) e OrderEntity (JPA).
 * 
 * <p>Converte entre entidades de domínio e entidades JPA, mantendo o domínio
 * isolado de detalhes de persistência.</p>
 * 
 * <h3>Arquitetura Hexagonal - Injeção de Dependência Explícita:</h3>
 * <ul>
 *   <li><strong>Component Spring:</strong> Gerenciado pelo Spring Container via @Component</li>
 *   <li><strong>Inversão de Controle:</strong> Injetado via construtor (SOLID - Dependency Inversion)</li>
 *   <li><strong>Controle Explícito:</strong> Código de mapeamento visível e manutenível</li>
 *   <li><strong>Consistência:</strong> Mesma abordagem do SagaExecutionRepositoryAdapter</li>
 * </ul>
 * 
 * <h3>Por que não usar MapStruct?</h3>
 * <ul>
 *   <li><strong>Controle Explícito:</strong> Código visível no projeto, não gerado</li>
 *   <li><strong>Flexibilidade:</strong> Fácil adicionar validações, logs, transformações</li>
 *   <li><strong>Debugging:</strong> Stack traces apontam para código real</li>
 *   <li><strong>Relacionamentos JPA:</strong> Controle total sobre mapeamento de coleções com orphanRemoval</li>
 *   <li><strong>Simplicidade:</strong> Sem dependência de annotation processing</li>
 * </ul>
 * 
 * <h3>Estratégia de Mapeamento:</h3>
 * <ul>
 *   <li><strong>Version:</strong> Ignorado no mapeamento (gerenciado pelo JPA)</li>
 *   <li><strong>Items:</strong> Mapeados separadamente para manter referência gerenciada pelo JPA</li>
 *   <li><strong>Update:</strong> Atualiza apenas campos simples, não itens (imutáveis após criação)</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Slf4j
@Component
public class OrderPersistenceMapper {
    
    /**
     * Converte Order (domínio) para OrderEntity (JPA).
     * 
     * <p>Ignora campos gerenciados pelo JPA (version) e mapeia itens separadamente
     * para manter referência gerenciada pelo JPA (importante para orphanRemoval).</p>
     * 
     * @param order Entidade de domínio
     * @return Entidade JPA
     */
    public OrderEntity toEntity(Order order) {
        if (order == null) {
            log.warn("Attempted to map null Order to entity");
            return null;
        }
        
        log.debug("Mapping Order to entity: id={}, orderNumber={}", order.getId(), order.getOrderNumber());
        
        OrderEntity entity = OrderEntity.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .customerId(order.getCustomerId())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .totalAmount(order.getTotalAmount())
            .paymentId(order.getPaymentId())
            .riskLevel(order.getRiskLevel())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            // version é ignorado - gerenciado pelo JPA
            // items são mapeados após criação da entidade
            .build();
        
        // Mapear itens após criação da entidade (necessário para referência bidirecional)
        mapItemsAfterMapping(order, entity);
        
        return entity;
    }
    
    /**
     * Mapeia os itens após a criação da OrderEntity.
     * 
     * <p>Necessário porque os itens precisam referenciar a OrderEntity pai.
     * Mantém referência gerenciada pelo JPA para evitar erros com orphanRemoval.</p>
     * 
     * @param order Entidade de domínio com itens
     * @param entity Entidade JPA a ser populada
     */
    private void mapItemsAfterMapping(Order order, OrderEntity entity) {
        // IMPORTANTE: Garantir que a lista está inicializada (já está com @Builder.Default, mas garantir)
        if (entity.getItems() == null) {
            entity.setItems(new ArrayList<>());
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
     * 
     * <p>Como Order tem items como final, precisamos construir o objeto completo
     * com todos os campos, incluindo a lista de itens.</p>
     * 
     * @param entity Entidade JPA
     * @return Entidade de domínio
     */
    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            log.warn("Attempted to map null OrderEntity to domain");
            return null;
        }
        
        log.debug("Mapping OrderEntity to domain: id={}, orderNumber={}", entity.getId(), entity.getOrderNumber());
        
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
     * <p>Atualiza apenas campos simples (status, paymentId, riskLevel, etc.).
     * Itens não são atualizados - eles são imutáveis após criação do pedido.</p>
     * 
     * @param order Entidade de domínio com dados atualizados
     * @param entity Entidade JPA a ser atualizada
     */
    public void updateEntity(Order order, OrderEntity entity) {
        if (order == null || entity == null) {
            log.warn("Attempted to update entity with null order or entity");
            return;
        }
        
        log.debug("Updating OrderEntity: id={}, orderNumber={}", entity.getId(), entity.getOrderNumber());
        
        // Atualizar apenas campos simples (não version, não items)
        entity.setOrderNumber(order.getOrderNumber());
        entity.setStatus(order.getStatus());
        entity.setCustomerId(order.getCustomerId());
        entity.setCustomerName(order.getCustomerName());
        entity.setCustomerEmail(order.getCustomerEmail());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setPaymentId(order.getPaymentId());
        entity.setRiskLevel(order.getRiskLevel());
        entity.setUpdatedAt(order.getUpdatedAt());
        
        // version é ignorado - gerenciado pelo JPA
        // items são ignorados - imutáveis após criação
    }
    
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
    public List<OrderItemEntity> mapItemsToEntity(List<OrderItem> items, OrderEntity order) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        
        log.debug("Mapping {} OrderItem(s) to entity", items.size());
        
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
    public List<OrderItem> mapItemsToDomain(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        
        log.debug("Mapping {} OrderItemEntity(s) to domain", items.size());
        
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

