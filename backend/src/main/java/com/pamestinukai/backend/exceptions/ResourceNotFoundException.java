package com.pamestinukai.backend.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    private String message;

    public ResourceNotFoundException() {}
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
