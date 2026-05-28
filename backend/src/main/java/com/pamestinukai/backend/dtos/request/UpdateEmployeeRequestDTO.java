package com.pamestinukai.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequestDTO {

    @Email
    @NotBlank
    private String email;

    private String phone;

    private boolean active;

    @Size(min = 8)
    private String password;
}
