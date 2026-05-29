package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.EmployeeResponseDTO;
import com.pamestinukai.backend.entities.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeResponseMapper {
    public EmployeeResponseDTO toDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setId(employee.getEmployeeId());
        dto.setVersion(employee.getVersion());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setActive(employee.isActive());
        dto.setCreatedAt(employee.getCreatedAt());

        Employee owner = employee.getOrganization() != null
                ? employee.getOrganization().getOwner()
                : null;
        dto.setOwner(owner != null && owner.getEmployeeId().equals(employee.getEmployeeId()));

        return dto;
    }
}
