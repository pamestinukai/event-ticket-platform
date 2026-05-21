package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.AuditoriumResponseDTO;
import com.pamestinukai.backend.entities.Auditorium;
import org.springframework.stereotype.Component;

@Component
public class AuditoriumResponseMapper {
    public AuditoriumResponseDTO toDTO(Auditorium auditorium) {
        AuditoriumResponseDTO dto = new AuditoriumResponseDTO();
        dto.setId(auditorium.getAuditoriumId());
        dto.setName(auditorium.getName());
        dto.setTotalCapacity(auditorium.getTotalCapacity());
        if (auditorium.getVenue() != null) {
            dto.setVenueId(auditorium.getVenue().getVenueId());
        }
        return dto;
    }
}
