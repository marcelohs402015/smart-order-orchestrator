package com.marcelo.orchestrator.infrastructure.persistence.mapper;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-06T15:51:29-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderEntity toEntity(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderEntity.OrderEntityBuilder orderEntity = OrderEntity.builder();

        orderEntity.id( order.getId() );
        orderEntity.orderNumber( order.getOrderNumber() );
        orderEntity.status( order.getStatus() );
        orderEntity.customerId( order.getCustomerId() );
        orderEntity.customerName( order.getCustomerName() );
        orderEntity.customerEmail( order.getCustomerEmail() );
        orderEntity.totalAmount( order.getTotalAmount() );
        orderEntity.paymentId( order.getPaymentId() );
        orderEntity.riskLevel( order.getRiskLevel() );
        orderEntity.createdAt( order.getCreatedAt() );
        orderEntity.updatedAt( order.getUpdatedAt() );

        return orderEntity.build();
    }

    @Override
    public Order toDomain(OrderEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.id( entity.getId() );
        order.orderNumber( entity.getOrderNumber() );
        order.status( entity.getStatus() );
        order.customerId( entity.getCustomerId() );
        order.customerName( entity.getCustomerName() );
        order.customerEmail( entity.getCustomerEmail() );
        order.totalAmount( entity.getTotalAmount() );
        order.paymentId( entity.getPaymentId() );
        order.riskLevel( entity.getRiskLevel() );
        order.createdAt( entity.getCreatedAt() );
        order.updatedAt( entity.getUpdatedAt() );

        order.items( mapItemsToDomain(entity.getItems()) );

        return order.build();
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
