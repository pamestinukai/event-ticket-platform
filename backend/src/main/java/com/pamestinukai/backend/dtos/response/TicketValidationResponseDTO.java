package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketValidationResponseDTO {
    private Long ticketId;
    private String eventTitle;
    private String eventDate;
    private String venue;
    private String buyerName;
    private String ticketType;
    private String status;
    private String token;
}
