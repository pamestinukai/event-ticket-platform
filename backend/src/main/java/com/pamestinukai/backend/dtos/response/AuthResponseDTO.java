package com.pamestinukai.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String email;
}
