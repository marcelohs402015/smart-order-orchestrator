package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de requisição para criação de cliente no AbacatePay.
 * 
 * <p>Representa os dados do cliente conforme esperado pela API do AbacatePay.
 * Baseado na documentação: POST /v1/customer/create</p>
 * 
 * <h3>Documentação AbacatePay:</h3>
 * <p>Endpoint: POST /v1/customer/create</p>
 * <p>Campos obrigatórios: name, cellphone, email, taxId</p>
 * 
 * @author Marcelo
 * @see <a href="https://docs.abacatepay.com/api-reference/criar-um-novo-cliente">AbacatePay Customer API</a>
 */
@Getter
@Builder
public class AbacatePayCustomerRequest {
    
    /**
     * Nome completo do cliente.
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * Celular do cliente.
     * Formato: "(11) 4002-8922"
     */
    @JsonProperty("cellphone")
    private String cellphone;
    
    /**
     * Email do cliente.
     */
    @JsonProperty("email")
    private String email;
    
    /**
     * CPF ou CNPJ do cliente.
     * Formato: "123.456.789-01" ou "12.345.678/0001-90"
     */
    @JsonProperty("taxId")
    private String taxId;
}

