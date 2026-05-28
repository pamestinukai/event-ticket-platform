package com.pamestinukai.backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCheckoutSessionResponseDTO {
    private String checkoutUrl;
}
