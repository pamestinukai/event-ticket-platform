package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.LoginRequestDTO;
import com.pamestinukai.backend.dtos.request.RegisterRequestDTO;
import com.pamestinukai.backend.dtos.response.AuthResponseDTO;

public interface IAuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
}
