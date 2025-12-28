package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreateOrderCommand {

    private final UUID customerId;

    private final String customerName;
    
    private final String customerEmail;

    private final List<OrderItem> items;
}

