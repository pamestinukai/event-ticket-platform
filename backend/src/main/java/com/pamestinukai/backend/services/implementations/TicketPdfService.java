package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generateTicketPdf(Ticket ticket, byte[] qrCodePng) {
        Event event = ticket.getTicketType().getEvent();
        Purchase purchase = ticket.getPurchase();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrCodePng));
            PDImageXObject qrPdImage = LosslessFactory.createFromImage(document, qrImage);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float left = 56;
                float y = 790;

                writeLine(content, "Pamestinukai Ticket", left, y, 18);
                y -= 34;

                writeLine(content, "Buyer: " + defaultValue(purchase.getBuyerName(), "Guest"), left, y, 12);
                y -= 18;
                writeLine(content, "Event: " + defaultValue(event.getTitle(), "Unknown event"), left, y, 12);
                y -= 18;
                writeLine(content, "Date: " + formatDate(event), left, y, 12);
                y -= 18;
                writeLine(content, "Venue: " + formatVenue(event), left, y, 12);
                y -= 18;
                writeLine(content, "Ticket type: " + defaultValue(ticket.getTicketType().getName(), "Unknown"), left, y, 12);
                y -= 18;
                writeLine(content, "Ticket token: " + defaultValue(ticket.getQrToken(), "N/A"), left, y, 12);

                content.drawImage(qrPdImage, 360, 520, 180, 180);
                writeLine(content, "Scan this QR code at the event entrance", 330, 500, 10);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate ticket PDF", e);
        }
    }

    private static void writeLine(PDPageContentStream content, String text, float x, float y, int fontSize) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private static String formatDate(Event event) {
        if (event.getStartDatetime() == null) {
            return "TBA";
        }
        return event.getStartDatetime().format(DATE_FORMAT);
    }

    private static String formatVenue(Event event) {
        if (event.getVenue() == null) {
            return "TBA";
        }

        String venueName = defaultValue(event.getVenue().getName(), "Unknown venue");
        String city = defaultValue(event.getVenue().getCity(), "");
        String address = defaultValue(event.getVenue().getAddress(), "");
        return (venueName + " " + city + " " + address).trim();
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
