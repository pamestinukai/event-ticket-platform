package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditoriumResponseDTO {
    private Long id;
    private String name;
    private int totalCapacity;
    private Long venueId;
}
