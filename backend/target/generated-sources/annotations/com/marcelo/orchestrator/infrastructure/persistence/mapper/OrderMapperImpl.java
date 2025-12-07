package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T08:33:02-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderEntity toEntity(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setId( order.getId() );
        orderEntity.setOrderNumber( order.getOrderNumber() );
        orderEntity.setStatus( order.getStatus() );
        orderEntity.setCustomerId( order.getCustomerId() );
        orderEntity.setCustomerName( order.getCustomerName() );
        orderEntity.setCustomerEmail( order.getCustomerEmail() );
        orderEntity.setTotalAmount( order.getTotalAmount() );
        orderEntity.setPaymentId( order.getPaymentId() );
        orderEntity.setRiskLevel( order.getRiskLevel() );
        orderEntity.setCreatedAt( order.getCreatedAt() );
        orderEntity.setUpdatedAt( order.getUpdatedAt() );

        mapItemsAfterMapping( order, orderEntity );

        return orderEntity;
    }

    @Override
    public void updateEntity(Order order, OrderEntity entity) {
        if ( order == null ) {
            return;
        }

        entity.setId( order.getId() );
        entity.setOrderNumber( order.getOrderNumber() );
        entity.setStatus( order.getStatus() );
        entity.setCustomerId( order.getCustomerId() );
        entity.setCustomerName( order.getCustomerName() );
        entity.setCustomerEmail( order.getCustomerEmail() );
        entity.setTotalAmount( order.getTotalAmount() );
        entity.setPaymentId( order.getPaymentId() );
        entity.setRiskLevel( order.getRiskLevel() );
        entity.setCreatedAt( order.getCreatedAt() );
        entity.setUpdatedAt( order.getUpdatedAt() );

        mapItemsAfterMapping( order, entity );
    }
}
