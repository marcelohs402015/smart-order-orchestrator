package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UpdateOrderStatusCommand {
    
    private final UUID orderId;
    private final OrderStatus newStatus;
}

