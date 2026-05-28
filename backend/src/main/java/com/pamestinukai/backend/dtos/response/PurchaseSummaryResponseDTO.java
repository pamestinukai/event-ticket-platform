package com.pamestinukai.backend.dtos.response;

import com.pamestinukai.backend.dtos.ReservedTicketSummaryDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PurchaseSummaryResponseDTO {
    private Long purchaseId;
    private String buyerName;
    private String buyerEmail;
    private BigDecimal totalPrice;
    private String currency;
    private String status;
    private Long eventId;
    private String eventName;
    private String eventStartDatetime;
    private String eventEndDatetime;
    private String venueName;
    private String venueCity;
    private String auditoriumName;
    private List<String> eventImages;
    private List<ReservedTicketSummaryDTO> tickets;
}
