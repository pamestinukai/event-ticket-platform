package com.pamestinukai.backend.services.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InlineImage {

    private final String contentId;
    private final byte[] data;
    private final String mimeType;

    public static InlineImage png(String contentId, byte[] data) {
        return new InlineImage(contentId, data, "image/png");
    }
}
