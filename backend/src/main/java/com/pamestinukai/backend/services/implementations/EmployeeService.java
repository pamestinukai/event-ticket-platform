package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.CreateEmployeeRequestDTO;
import com.pamestinukai.backend.dtos.request.UpdateEmployeeRequestDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.EmployeeRepository;
import com.pamestinukai.backend.services.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Employee> getOrganizationEmployees(Long organizationId) {
        return employeeRepository.findByOrganization_OrganizationIdOrderByCreatedAtAsc(organizationId);
    }

    public Employee createEmployee(Organization organization, CreateEmployeeRequestDTO request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Employee employee = new Employee();
        employee.setOrganization(organization);
        employee.setEmail(request.getEmail());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setPhone(request.getPhone());
        employee.setActive(true);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Organization organization, Long employeeId, UpdateEmployeeRequestDTO request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getOrganization() == null
                || !employee.getOrganization().getOrganizationId().equals(organization.getOrganizationId())) {
            throw new ResourceNotFoundException("Employee not found");
        }

        if (request.getVersion() != null && !request.getVersion().equals(employee.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Employee.class, employeeId);
        }

        boolean isOwner = organization.getOwner() != null
                && organization.getOwner().getEmployeeId().equals(employee.getEmployeeId());
        if (isOwner && !request.isActive()) {
            throw new IllegalArgumentException("Cannot deactivate the organization owner");
        }

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setActive(request.isActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }
}
