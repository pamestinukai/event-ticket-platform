package com.pamestinukai.backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private String errorMessage;
    private Map<String, String> details;

    public ErrorResponse(String message) {
        this.errorMessage = message;
        this.details = null;
    }
}
