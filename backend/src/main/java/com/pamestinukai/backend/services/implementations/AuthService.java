package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.LoginRequestDTO;
import com.pamestinukai.backend.dtos.request.RegisterRequestDTO;
import com.pamestinukai.backend.dtos.response.AuthResponseDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Organization;
import com.pamestinukai.backend.repositories.EmployeeRepository;
import com.pamestinukai.backend.repositories.OrganizationRepository;
import com.pamestinukai.backend.services.JwtService;
import com.pamestinukai.backend.services.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Employee employee = employeeRepository.findByEmail(request.getEmail()).orElseThrow();
        return new AuthResponseDTO(jwtService.generateToken(employee), employee.getEmail());
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Employee employee = new Employee();
        employee.setEmail(request.getEmail());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setPhone(request.getPhone());
        employee.setActive(true);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);

        String companyName = (request.getOrganizationName() != null && !request.getOrganizationName().isBlank())
                ? request.getOrganizationName()
                : request.getEmail().substring(0, request.getEmail().indexOf('@')) + "'s Company (" + request.getEmail().substring(request.getEmail().indexOf('@') + 1) + ")";

        if (organizationRepository.existsByCompanyName(companyName)) {
            throw new IllegalArgumentException("Organization name already in use");
        }

        Organization org = new Organization();
        org.setOwner(employee);
        org.setCompanyName(companyName);
        org.setActive(true);
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        organizationRepository.save(org);

        employee.setOrganization(org);
        employeeRepository.save(employee);

        return new AuthResponseDTO(jwtService.generateToken(employee), employee.getEmail());
    }
}
