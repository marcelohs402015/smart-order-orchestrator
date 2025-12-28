package com.marcelo.orchestrator.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Customer {
    

    private final UUID id;
    
    private final String name;
    
    private final String email;
    
    private final String phone;
    
    private final Address address;
    
    public boolean hasValidEmail() {
        return email != null && !email.isBlank() && email.contains("@");
    }
    
    public boolean hasCompleteAddress() {
        return address != null && address.isComplete();
    }
}

