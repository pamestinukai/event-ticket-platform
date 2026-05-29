package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.CreateEmployeeRequestDTO;
import com.pamestinukai.backend.dtos.request.UpdateEmployeeRequestDTO;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.entities.Organization;

import java.util.List;

public interface IEmployeeService {
    List<Employee> getOrganizationEmployees(Long organizationId);
    Employee createEmployee(Organization organization, CreateEmployeeRequestDTO request);
    Employee updateEmployee(Organization organization, Long employeeId, UpdateEmployeeRequestDTO request);
}
