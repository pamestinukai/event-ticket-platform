package com.pamestinukai.backend.exceptions;

public class InvalidTicketTokenException extends RuntimeException {
    public InvalidTicketTokenException(String message) {
        super(message);
    }
}
