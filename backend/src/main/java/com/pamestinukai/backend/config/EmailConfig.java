package com.pamestinukai.backend.config;

import com.pamestinukai.backend.services.interfaces.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration @Slf4j
public class EmailConfig {

    @Value("${app.email.provider:smtp}")
    private String provider;

    @Bean @Primary
    public IEmailService emailService(Map<String, IEmailService> implementations) {
        IEmailService selected = implementations.get(provider);
        if (selected == null) throw new IllegalStateException(
                "No IEmailService bean named '" + provider + "'. Available: " + implementations.keySet());
        log.info("Active email provider: {}", provider);
        return selected;
    }
}