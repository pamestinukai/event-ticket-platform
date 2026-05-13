package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository  extends JpaRepository<Venue, Long> {

}
