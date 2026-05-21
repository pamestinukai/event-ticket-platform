package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.response.TicketTypeResponseDTO;
import com.pamestinukai.backend.entities.TicketType;
import org.springframework.stereotype.Component;

@Component
public class TicketTypeResponseMapper {
    public TicketTypeResponseDTO toDTO(TicketType ticketType) {
        TicketTypeResponseDTO dto = new TicketTypeResponseDTO();
        dto.setId(ticketType.getTicketTypeId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        dto.setTotalQuantity(ticketType.getTotalQuantity());
        dto.setAvailableQuantity(ticketType.getAvailableQuantity());
        return dto;
    }
}
