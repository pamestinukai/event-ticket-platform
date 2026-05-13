package com.pamestinukai.backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRequestDTO {
    @NotNull(message = "Owner is required")
    private Long ownerId;

    @NotBlank(message = "Company name must not be empty")
    @Size(max = 100, message = "Company name has to be up to 100 characters long")
    private String companyName;

    private boolean active;
}

