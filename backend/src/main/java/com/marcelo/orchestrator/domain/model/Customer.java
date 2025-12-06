package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Entidade de domínio que representa um cliente.
 * 
 * <p>Implementa o padrão <strong>Rich Domain Model</strong> do DDD.
 * Esta entidade contém informações do cliente necessárias para processamento de pedidos.</p>
 * 
 * <h3>Por que Entidade e não Value Object?</h3>
 * <ul>
 *   <li><strong>Identidade:</strong> Cliente tem identidade única (UUID) que persiste ao longo do tempo</li>
 *   <li><strong>Mutação:</strong> Dados do cliente podem mudar (email, endereço) mas mantém a mesma identidade</li>
 *   <li><strong>Histórico:</strong> Pode ter relacionamentos com múltiplos pedidos</li>
 * </ul>
 * 
 * <h3>Snapshot vs. Referência:</h3>
 * <p>No contexto de um pedido, podemos armazenar um "snapshot" dos dados do cliente
 * (customerName, customerEmail) para manter histórico, mesmo que o cliente atualize
 * seus dados depois. Isso garante que o pedido sempre reflete os dados no momento da compra.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class Customer {
    
    /**
     * Identificador único do cliente (UUID).
     * Imutável após criação - garante identidade única.
     */
    private final UUID id;
    
    /**
     * Nome completo do cliente.
     */
    private final String name;
    
    /**
     * Email do cliente (usado para comunicação e notificações).
     */
    private final String email;
    
    /**
     * Telefone do cliente (opcional).
     */
    private final String phone;
    
    /**
     * Endereço do cliente.
     * Value Object que encapsula informações de localização.
     */
    private final Address address;
    
    /**
     * Verifica se o cliente tem email válido.
     * 
     * <p>Validação básica de negócio encapsulada no domínio.
     * Validações mais complexas podem ser adicionadas aqui.</p>
     * 
     * @return {@code true} se email não é nulo e não está vazio
     */
    public boolean hasValidEmail() {
        return email != null && !email.isBlank() && email.contains("@");
    }
    
    /**
     * Verifica se o cliente tem endereço completo.
     * 
     * @return {@code true} se endereço não é nulo e tem informações básicas
     */
    public boolean hasCompleteAddress() {
        return address != null && address.isComplete();
    }
}

