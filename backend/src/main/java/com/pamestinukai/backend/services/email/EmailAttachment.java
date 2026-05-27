package com.pamestinukai.backend.services.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailAttachment {

    private final String filename;
    private final byte[] data;
    private final String mimeType;

    public static EmailAttachment pdf(String filename, byte[] data) {
        return new EmailAttachment(filename, data, "application/pdf");
    }
}
