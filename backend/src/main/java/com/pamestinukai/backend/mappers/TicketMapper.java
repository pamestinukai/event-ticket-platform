package com.pamestinukai.backend.mappers;

import com.pamestinukai.backend.dtos.ReservedTicketSummaryDTO;
import com.pamestinukai.backend.dtos.TicketReservationItemDTO;
import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TicketMapper {

    public ReservedTicketSummaryDTO toReservedTicketSummaryDTO(TicketReservationItemDTO item, TicketType ticketType) {
        ReservedTicketSummaryDTO summary = new ReservedTicketSummaryDTO();
        summary.setTicketTypeId(ticketType.getTicketTypeId());
        summary.setTicketTypeName(ticketType.getName());
        summary.setQuantity(item.getQuantity());
        summary.setPricePerTicket(ticketType.getPrice());
        return summary;
    }

    public List<ReservedTicketSummaryDTO> toReservedTicketSummaryDTOs(List<TicketReservationItemDTO> items, Map<Long, TicketType> ticketTypeMap) {
        List<ReservedTicketSummaryDTO> summaries = new ArrayList<>();
        for (TicketReservationItemDTO item : items) {
            TicketType tt = ticketTypeMap.get(item.getTicketTypeId());
            if (tt == null) throw new ResourceNotFoundException("TicketType not found");
            summaries.add(toReservedTicketSummaryDTO(item, tt));
        }
        return summaries;
    }

    public TicketReservationResponseDTO toReservationResponseDTO(
            TicketReservationRequestDTO dto,
            Event event,
            Map<Long, TicketType> ticketTypeMap,
            Purchase purchase,
            BigDecimal totalPrice
    ) {
        TicketReservationResponseDTO responseDTO = new TicketReservationResponseDTO();
        responseDTO.setPurchaseId(purchase.getPurchaseId());
        responseDTO.setEventId(dto.getEventId());
        responseDTO.setEventName(event.getTitle());
        responseDTO.setTickets(toReservedTicketSummaryDTOs(dto.getTickets(), ticketTypeMap));
        responseDTO.setCurrency("EUR");
        responseDTO.setTotalPrice(totalPrice);
        responseDTO.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return responseDTO;
    }
}
