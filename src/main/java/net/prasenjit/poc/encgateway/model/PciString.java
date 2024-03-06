package net.prasenjit.poc.encgateway.model;

public record PciString(String value) {
    public PciString {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }
}
