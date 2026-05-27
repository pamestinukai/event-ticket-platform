package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;

public interface ITicketService {
    TicketReservationResponseDTO reserveTicket(TicketReservationRequestDTO dto);
    void cancelTicketReservation(Long purchaseId);
    void confirmTicketReservation(Long purchaseId);
}
