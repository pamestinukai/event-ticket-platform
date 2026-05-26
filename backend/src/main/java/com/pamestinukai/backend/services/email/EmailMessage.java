package com.pamestinukai.backend.services.email;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class EmailMessage {

    private final String to;
    private final String subject;
    private final String templateName;

    @Singular("variable")
    private final Map<String, Object> variables;

    @Singular
    private final List<InlineImage> inlineImages;

    @Singular
    private final List<EmailAttachment> attachments;
}
