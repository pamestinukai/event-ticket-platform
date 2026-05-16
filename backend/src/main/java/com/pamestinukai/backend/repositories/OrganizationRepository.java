package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByCompanyName(String companyName);

}
