package com.marcelo.orchestrator.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO representing a single product item in AbacatePay billing request.
 *
 * <p>Based on AbacatePay documentation for POST /v1/billing/create.</p>
 */
@Getter
@Builder
public class AbacatePayProductRequest {

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * Unit price in cents.
     */
    @JsonProperty("price")
    private Integer price;
}


