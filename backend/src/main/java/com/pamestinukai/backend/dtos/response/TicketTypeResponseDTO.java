package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TicketTypeResponseDTO {
    private Long id;
    private Long version;
    private String name;
    private BigDecimal price;
    private int totalQuantity;
    private int availableQuantity;
}
