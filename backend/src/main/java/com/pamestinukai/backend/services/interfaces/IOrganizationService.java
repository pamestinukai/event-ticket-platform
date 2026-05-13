package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.OrganizationRequestDTO;
import com.pamestinukai.backend.entities.Organization;

import java.util.List;

public interface IOrganizationService {
    List<Organization> getAllOrganizations();
    Organization getOrganizationById(Long id);
    Organization createOrganization(OrganizationRequestDTO organizationRequestDTO);
    Organization updateOrganization(Long id, OrganizationRequestDTO organizationRequestDTO);
    void deleteOrganization(Long id);

}
