package com.marcelo.orchestrator.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;


public final class OrderNumber {
    
    private static final String PREFIX = "ORD-";
    
    private static final Pattern VALID_PATTERN = Pattern.compile("^ORD-\\d+$");

    private final String value;
    
    private OrderNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order number cannot be null or blank");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException(
                String.format("Invalid order number format: %s. Expected format: ORD-<numbers>", value)
            );
        }
        this.value = value;
    }
    
    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }
    
    public static OrderNumber generate() {
        long timestamp = System.currentTimeMillis();
        return new OrderNumber(PREFIX + timestamp);
    }
    
    public static OrderNumber generate(long suffix) {
        return new OrderNumber(PREFIX + suffix);
    }
    
    private static boolean isValidFormat(String value) {
        return VALID_PATTERN.matcher(value).matches();
    }

    public String getValue() {
        return value;
    }
    
    public long getNumericSuffix() {
        String suffix = value.substring(PREFIX.length());
        return Long.parseLong(suffix);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

