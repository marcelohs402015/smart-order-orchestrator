package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Address {
    

    private final String street;

    private final String number;
    
    private final String complement;
    
    private final String neighborhood;
    
    private final String city;
    
    private final String state;
    
    private final String zipCode;
    
    @Builder.Default
    private final String country = "Brasil";
    
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
    
    public boolean isComplete() {
        return street != null && !street.isBlank()
            && number != null && !number.isBlank()
            && city != null && !city.isBlank()
            && state != null && !state.isBlank()
            && zipCode != null && !zipCode.isBlank();
    }
}

