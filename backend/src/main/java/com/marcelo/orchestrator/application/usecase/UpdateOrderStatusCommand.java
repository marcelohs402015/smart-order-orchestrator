package com.marcelo.orchestrator.application.usecase;

import com.marcelo.orchestrator.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Command para atualização de status de pedido.
 * 
 * <p>Contém dados necessários para atualizar status de um pedido.</p>
 * 
 * @param orderId ID do pedido a atualizar
 * @param newStatus Novo status desejado
 * @author Marcelo
 */
@Getter
@Builder
public class UpdateOrderStatusCommand {
    
    private final UUID orderId;
    private final OrderStatus newStatus;
}

