package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.CreateEmployeeRequestDTO;
import com.pamestinukai.backend.dtos.request.UpdateEmployeeRequestDTO;
import com.pamestinukai.backend.dtos.response.EmployeeResponseDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.mappers.EmployeeResponseMapper;
import com.pamestinukai.backend.services.interfaces.IEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final EmployeeResponseMapper employeeResponseMapper;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getOrganizationEmployees(
            @AuthenticationPrincipal Employee principal) {
        Organization organization = requireOwnerOrganization(principal);
        List<EmployeeResponseDTO> employees = employeeService
                .getOrganizationEmployees(organization.getOrganizationId())
                .stream()
                .map(employeeResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @AuthenticationPrincipal Employee principal,
            @Valid @RequestBody CreateEmployeeRequestDTO request) {
        Organization organization = requireOwnerOrganization(principal);
        Employee employee = employeeService.createEmployee(organization, request);
        return ResponseEntity.status(201).body(employeeResponseMapper.toDTO(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @AuthenticationPrincipal Employee principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequestDTO request) {
        Organization organization = requireOwnerOrganization(principal);
        Employee employee = employeeService.updateEmployee(organization, id, request);
        return ResponseEntity.ok(employeeResponseMapper.toDTO(employee));
    }

    private Organization requireOwnerOrganization(Employee principal) {
        if (principal == null || principal.getOrganization() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organization");
        }
        Organization organization = principal.getOrganization();
        Employee owner = organization.getOwner();
        if (owner == null || !owner.getEmployeeId().equals(principal.getEmployeeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the organization owner can manage employees");
        }
        return organization;
    }
}
