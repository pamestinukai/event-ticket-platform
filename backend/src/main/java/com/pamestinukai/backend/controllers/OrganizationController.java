package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.OrganizationRequestDTO;
import com.pamestinukai.backend.dtos.response.OrganizationResponseDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.services.implementations.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.pamestinukai.backend.mappers.OrganizationResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OrganizationResponseMapper organizationResponseMapper;

    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationResponseDTO> organizationResponseDTOS = organizations.stream()
                .map(organizationResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(organizationResponseDTOS);
    }

    @GetMapping("/me")
    public ResponseEntity<OrganizationResponseDTO> getMyOrganization(@AuthenticationPrincipal Employee employee) {
        if (employee == null || employee.getOrganization() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(organizationResponseMapper.toDTO(employee.getOrganization()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> getOrganizationById(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        OrganizationResponseDTO organizationResponseDTO = organizationResponseMapper.toDTO(organization);
        return ResponseEntity.ok(organizationResponseDTO);
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(@Valid @RequestBody OrganizationRequestDTO organizationRequestDTO) {
        Organization organization = organizationService.createOrganization(organizationRequestDTO);
        OrganizationResponseDTO organizationResponseDTO = organizationResponseMapper.toDTO(organization);
        return ResponseEntity.status(201).body(organizationResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationRequestDTO organizationRequestDTO) {
        Organization organization = organizationService.updateOrganization(id, organizationRequestDTO);
        OrganizationResponseDTO organizationResponseDTO = organizationResponseMapper.toDTO(organization);
        return ResponseEntity.ok(organizationResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
