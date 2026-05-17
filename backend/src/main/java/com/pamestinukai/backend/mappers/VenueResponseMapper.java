package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.VenueResponseDTO;
import com.pamestinukai.backend.entities.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueResponseMapper {
    public VenueResponseDTO toDTO(Venue venue) {
        VenueResponseDTO venueResponseDTO = new VenueResponseDTO();

        venueResponseDTO.setName(venue.getName());
        venueResponseDTO.setAddress(venue.getAddress());
        venueResponseDTO.setCity(venue.getCity());
        venueResponseDTO.setCountry(venue.getCountry());
        return venueResponseDTO;
    }
}
