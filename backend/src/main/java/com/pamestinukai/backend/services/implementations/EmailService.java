package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.services.email.EmailAttachment;
import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.email.InlineImage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromAddress, fromName);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(renderHtml(message.getTemplateName(), message.getVariables()), true);

            for (InlineImage image : safe(message.getInlineImages())) {
                ByteArrayDataSource dataSource = new ByteArrayDataSource(image.getData(), image.getMimeType());
                helper.addInline(image.getContentId(), dataSource);
            }

            for (EmailAttachment attachment : safe(message.getAttachments())) {
                ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment.getData(), attachment.getMimeType());
                helper.addAttachment(attachment.getFilename(), dataSource);
            }

            mailSender.send(mime);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to send email to " + message.getTo(), e);
        }
    }

    private String renderHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return templateEngine.process("email/" + templateName, context);
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
