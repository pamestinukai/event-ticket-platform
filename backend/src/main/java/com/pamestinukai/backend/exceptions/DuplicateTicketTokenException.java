package com.pamestinukai.backend.exceptions;

public class DuplicateTicketTokenException extends RuntimeException {
    public DuplicateTicketTokenException(String message) {
        super(message);
    }
}
