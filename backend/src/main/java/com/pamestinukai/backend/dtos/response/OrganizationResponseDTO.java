package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrganizationResponseDTO {
    private Long id;
    private String ownerEmail;
    private String companyName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
