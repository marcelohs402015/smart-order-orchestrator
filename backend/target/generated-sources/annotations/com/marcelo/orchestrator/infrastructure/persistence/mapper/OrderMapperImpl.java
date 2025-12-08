package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-08T16:39:41-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderEntity toEntity(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderEntity.OrderEntityBuilder orderEntity = OrderEntity.builder();

        orderEntity.createdAt( order.getCreatedAt() );
        orderEntity.customerEmail( order.getCustomerEmail() );
        orderEntity.customerId( order.getCustomerId() );
        orderEntity.customerName( order.getCustomerName() );
        orderEntity.id( order.getId() );
        orderEntity.orderNumber( order.getOrderNumber() );
        orderEntity.paymentId( order.getPaymentId() );
        orderEntity.riskLevel( order.getRiskLevel() );
        orderEntity.status( order.getStatus() );
        orderEntity.totalAmount( order.getTotalAmount() );
        orderEntity.updatedAt( order.getUpdatedAt() );

        return orderEntity.build();
    }

    @Override
    public void updateEntity(Order order, OrderEntity entity) {
        if ( order == null ) {
            return;
        }

        entity.setCreatedAt( order.getCreatedAt() );
        entity.setCustomerEmail( order.getCustomerEmail() );
        entity.setCustomerId( order.getCustomerId() );
        entity.setCustomerName( order.getCustomerName() );
        entity.setId( order.getId() );
        entity.setOrderNumber( order.getOrderNumber() );
        entity.setPaymentId( order.getPaymentId() );
        entity.setRiskLevel( order.getRiskLevel() );
        entity.setStatus( order.getStatus() );
        entity.setTotalAmount( order.getTotalAmount() );
        entity.setUpdatedAt( order.getUpdatedAt() );

        mapItemsAfterMapping( order, entity );
    }
}
