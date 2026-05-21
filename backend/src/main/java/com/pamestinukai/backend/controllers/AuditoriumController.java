package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.response.AuditoriumResponseDTO;
import com.pamestinukai.backend.mappers.AuditoriumResponseMapper;
import com.pamestinukai.backend.repositories.AuditoriumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auditoriums")
public class AuditoriumController {
    private final AuditoriumRepository auditoriumRepository;
    private final AuditoriumResponseMapper auditoriumResponseMapper;

    @GetMapping
    public ResponseEntity<List<AuditoriumResponseDTO>> getAllAuditoriums() {
        List<AuditoriumResponseDTO> auditoriums = auditoriumRepository.findAll().stream()
                .map(auditoriumResponseMapper::toDTO)
                .toList();
        return ResponseEntity.ok(auditoriums);
    }
}
