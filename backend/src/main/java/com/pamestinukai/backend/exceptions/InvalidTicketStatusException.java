package com.pamestinukai.backend.exceptions;

public class InvalidTicketStatusException extends RuntimeException {
    public InvalidTicketStatusException(String message) {
        super(message);
    }
}
