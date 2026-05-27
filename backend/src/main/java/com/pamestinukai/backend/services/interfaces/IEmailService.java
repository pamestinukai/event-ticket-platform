package com.pamestinukai.backend.services.interfaces;

import com.pamestinukai.backend.services.email.EmailMessage;

public interface IEmailService {
    void send(EmailMessage message);
}
