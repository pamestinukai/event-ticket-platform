package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.TicketReservationRequestDTO;
import com.pamestinukai.backend.dtos.response.TicketReservationResponseDTO;
import com.pamestinukai.backend.dtos.response.TicketValidationResponseDTO;

public interface ITicketService {
    TicketReservationResponseDTO reserveTicket(TicketReservationRequestDTO dto);
    void cancelTicketReservation(Long purchaseId);
    void confirmTicketReservation(Long purchaseId);
    void confirmTicketReservation(Long purchaseId, String buyerEmail, String buyerName);
    TicketValidationResponseDTO validateTicketToken(String qrToken);
    TicketValidationResponseDTO checkInTicket(String qrToken);
}
