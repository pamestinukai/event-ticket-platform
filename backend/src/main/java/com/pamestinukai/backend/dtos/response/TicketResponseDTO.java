package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketResponseDTO {
    private Long ticketId;
    private String qrToken;
    private String ticketTypeName;
    private String status;
}