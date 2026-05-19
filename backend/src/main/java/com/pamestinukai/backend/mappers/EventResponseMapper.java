package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.EventResponseDTO;
import com.pamestinukai.backend.dtos.response.OrganizationResponseDTO;
import com.pamestinukai.backend.dtos.response.VenueResponseDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.repositories.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EventResponseMapper {
    private final OrganizationResponseMapper organizationResponseMapper;
    private final VenueResponseMapper venueResponseMapper;
    private final TicketTypeRepository typeRepository;

    public EventResponseDTO toDTO(Event event){
        EventResponseDTO dto = new EventResponseDTO();
        dto.setEventId(event.getEventId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setPerformers(event.getPerformers());
        dto.setImages(event.getImages());
        dto.setStartDatetime(event.getStartDatetime());
        dto.setEndDatetime(event.getEndDatetime());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        OrganizationResponseDTO organizationResponseDTO = organizationResponseMapper.toDTO(event.getOrganization());
        dto.setOrganization(organizationResponseDTO);

        VenueResponseDTO venueResponseDTO = venueResponseMapper.toDTO(event.getVenue());
        dto.setVenue(venueResponseDTO);


        dto.setStartingTicketPrice(
                typeRepository.findByEvent(event)
                        .stream()
                        .map(TicketType::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(null)
        );
        dto.setAuditoriumName(event.getAuditorium().getName());
        dto.setCategoryName(event.getCategory().getName());
        return dto;
    }
}
