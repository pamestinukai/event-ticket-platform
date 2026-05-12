package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.OrganizationRequestDTO;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.repositories.EmployeeRepository;
import com.pamestinukai.backend.repositories.OrganizationRepository;
import com.pamestinukai.backend.services.interfaces.IOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService implements IOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;

    public List<Organization> getAllOrganizations(){
        return organizationRepository.findAll();
    }
    public Organization getOrganizationById(Long id){
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }
    public Organization createOrganization(OrganizationRequestDTO organizationRequestDTO){
        Organization organization = mapToEntity(new Organization(), organizationRequestDTO);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());
        return organizationRepository.save(organization);
    }
    public Organization updateOrganization(Long id, OrganizationRequestDTO organizationRequestDTO){
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        mapToEntity(organization, organizationRequestDTO);
        organization.setUpdatedAt(LocalDateTime.now());
        return organizationRepository.save(organization);
    }
    public void deleteOrganization(Long id){
        organizationRepository.deleteById(id);
    }

    private Organization mapToEntity(Organization organization, OrganizationRequestDTO dto) {
        organization.setCompanyName(dto.getCompanyName());
        organization.setActive(dto.isActive());
        if (dto.getOwnerId() != null)
            organization.setOwner(employeeRepository.findById(dto.getOwnerId())
                                .orElseThrow(() -> new RuntimeException("Owner not found")));
        return organization;
    }
}
