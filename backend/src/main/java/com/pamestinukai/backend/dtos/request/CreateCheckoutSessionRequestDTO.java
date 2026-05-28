package com.pamestinukai.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCheckoutSessionRequestDTO {

    @NotNull(message = "Purchase ID is required")
    private Long purchaseId;

    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @NotBlank(message = "Buyer email is required")
    @Email(message = "Invalid email address")
    private String buyerEmail;
}
