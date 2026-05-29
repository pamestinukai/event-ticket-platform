package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeResponseDTO {
    private Long id;
    private Long version;
    private String email;
    private String phone;
    private boolean active;
    private boolean owner;
    private LocalDateTime createdAt;
}
