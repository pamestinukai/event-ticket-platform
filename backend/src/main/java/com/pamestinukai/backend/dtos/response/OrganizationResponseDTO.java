package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationResponseDTO {
    private Long id;
    private String ownerEmail;
    private String ownerPhone;
    private String companyName;
}
