package com.pamestinukai.backend.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRequestDTO {
    private Long ownerId;
    private String companyName;
    private boolean active;
}

