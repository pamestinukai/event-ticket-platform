package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByOrganization_OrganizationIdOrderByCreatedAtAsc(Long organizationId);
}
