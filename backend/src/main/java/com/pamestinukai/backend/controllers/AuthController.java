package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.dtos.request.LoginRequestDTO;
import com.pamestinukai.backend.dtos.request.RegisterRequestDTO;
import com.pamestinukai.backend.dtos.response.AuthResponseDTO;
import com.pamestinukai.backend.services.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }
}
