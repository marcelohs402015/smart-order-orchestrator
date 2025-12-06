package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Value Object que representa um endereço.
 * 
 * <p>Implementa o padrão <strong>Value Object</strong> do DDD.
 * Value Objects são definidos apenas por seus atributos, não por identidade.
 * Dois endereços com os mesmos valores são considerados iguais.</p>
 * 
 * <h3>Por que Value Object?</h3>
 * <ul>
 *   <li><strong>Imutabilidade:</strong> Endereço não muda após criação</li>
 *   <li><strong>Sem Identidade:</strong> Dois endereços idênticos são o mesmo endereço</li>
 *   <li><strong>Encapsulamento:</strong> Lógica relacionada a endereço fica aqui (formatação, validação)</li>
 * </ul>
 * 
 * <h3>Uso no Domínio:</h3>
 * <p>Endereço é usado em Customer e pode ser usado em Order (endereço de entrega).
 * Como Value Object, pode ser compartilhado sem problemas de referência.</p>
 * 
 * @author Marcelo
 */
@Getter
@Builder
public class Address {
    
    /**
     * Rua/Logradouro.
     */
    private final String street;
    
    /**
     * Número do endereço.
     */
    private final String number;
    
    /**
     * Complemento (apto, bloco, etc.) - opcional.
     */
    private final String complement;
    
    /**
     * Bairro.
     */
    private final String neighborhood;
    
    /**
     * Cidade.
     */
    private final String city;
    
    /**
     * Estado (UF).
     */
    private final String state;
    
    /**
     * CEP (Código Postal).
     */
    private final String zipCode;
    
    /**
     * País (padrão: Brasil).
     */
    @Builder.Default
    private final String country = "Brasil";
    
    /**
     * Retorna o endereço formatado completo.
     * 
     * <p>Encapsula a lógica de formatação no próprio Value Object.
     * Isso segue o princípio de "Tell, Don't Ask" - o objeto sabe como se formatar.</p>
     * 
     * @return String com endereço formatado (ex: "Rua Exemplo, 123 - Apto 45, Centro, São Paulo - SP, 01234-567")
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (street != null && !street.isBlank()) {
            sb.append(street);
        }
        
        if (number != null && !number.isBlank()) {
            sb.append(", ").append(number);
        }
        
        if (complement != null && !complement.isBlank()) {
            sb.append(" - ").append(complement);
        }
        
        if (neighborhood != null && !neighborhood.isBlank()) {
            sb.append(", ").append(neighborhood);
        }
        
        if (city != null && !city.isBlank()) {
            sb.append(", ").append(city);
        }
        
        if (state != null && !state.isBlank()) {
            sb.append(" - ").append(state);
        }
        
        if (zipCode != null && !zipCode.isBlank()) {
            sb.append(", ").append(zipCode);
        }
        
        if (country != null && !country.isBlank()) {
            sb.append(", ").append(country);
        }
        
        return sb.toString();
    }
    
    /**
     * Verifica se o endereço está completo (tem informações mínimas necessárias).
     * 
     * <p>Validação de negócio encapsulada no Value Object.
     * Define quais campos são obrigatórios para um endereço válido.</p>
     * 
     * @return {@code true} se endereço tem informações mínimas (rua, número, cidade, estado, CEP)
     */
    public boolean isComplete() {
        return street != null && !street.isBlank()
            && number != null && !number.isBlank()
            && city != null && !city.isBlank()
            && state != null && !state.isBlank()
            && zipCode != null && !zipCode.isBlank();
    }
}

