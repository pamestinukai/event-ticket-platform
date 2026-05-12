package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.OrganizationRequestDTO;
import com.pamestinukai.backend.dtos.response.OrganizationResponseDTO;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.services.implementations.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationResponseDTO> organizationResponseDTOS = organizations.stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(organizationResponseDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> getOrganizationById(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        OrganizationResponseDTO organizationResponseDTO = mapToDTO(organization);
        return ResponseEntity.ok(organizationResponseDTO);
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO) {
        Organization organization = organizationService.createOrganization(organizationRequestDTO);
        OrganizationResponseDTO organizationResponseDTO = mapToDTO(organization);
        return ResponseEntity.status(201).body(organizationResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationRequestDTO organizationRequestDTO) {
        Organization organization = organizationService.updateOrganization(id, organizationRequestDTO);
        OrganizationResponseDTO organizationResponseDTO = mapToDTO(organization);
        return ResponseEntity.ok(organizationResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    private OrganizationResponseDTO mapToDTO(Organization organization) {
        OrganizationResponseDTO organizationResponseDTO = new OrganizationResponseDTO();

        organizationResponseDTO.setId(organization.getOrganizationId());
        organizationResponseDTO.setCompanyName(organization.getCompanyName());
        organizationResponseDTO.setOwnerEmail(organization.getOwner().getEmail());
        organizationResponseDTO.setActive(organization.getOwner().isActive());
        organizationResponseDTO.setCreatedAt(organization.getCreatedAt());
        organizationResponseDTO.setUpdatedAt(organization.getUpdatedAt());

        return organizationResponseDTO;
    }
}
