package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.dtos.request.CreateCheckoutSessionRequestDTO;
import com.pamestinukai.backend.dtos.response.CreateCheckoutSessionResponseDTO;
import com.stripe.exception.StripeException;

public interface IStripeService {
    CreateCheckoutSessionResponseDTO createCheckoutSession(CreateCheckoutSessionRequestDTO dto) throws StripeException;
    void handleWebhookEvent(String payload, String sigHeader);
    void syncSessionIfPaid(com.pamestinukai.backend.entities.Purchase purchase);
}
