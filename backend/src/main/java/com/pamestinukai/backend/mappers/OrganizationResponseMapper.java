package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.OrganizationResponseDTO;
import com.pamestinukai.backend.entities.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationResponseMapper {
    public OrganizationResponseDTO toDTO(Organization organization) {
        OrganizationResponseDTO dto = new OrganizationResponseDTO();

        dto.setId(organization.getOrganizationId());
        dto.setCompanyName(organization.getCompanyName());
        dto.setOwnerEmail(organization.getOwner().getEmail());
        dto.setOwnerPhone(organization.getOwner().getPhone());

        return dto;
    }
}
