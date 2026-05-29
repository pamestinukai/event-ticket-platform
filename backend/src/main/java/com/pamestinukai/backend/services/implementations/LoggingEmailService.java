package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.services.email.EmailAttachment;
import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.email.InlineImage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("logging")
@RequiredArgsConstructor
public class LoggingEmailService implements IEmailService {

    private final SpringTemplateEngine templateEngine;

    @Override
    public void send(EmailMessage message) {
        String renderedHtml = renderHtml(message.getTemplateName(), message.getVariables());

        log.info("""
                ╔══════════════════════════════════════════════════════════
                ║  [EMAIL — NOT SENT — logging provider active]
                ║  To:       {}
                ║  Subject:  {}
                ║  Template: {}
                ╠══════════════════════════════════════════════════════════
                {}
                ╚══════════════════════════════════════════════════════════""",
                message.getTo(),
                message.getSubject(),
                message.getTemplateName(),
                renderedHtml);

        logAttachments("Inline image", safe(message.getInlineImages())
                .stream().map(InlineImage::getContentId).toList());

        logAttachments("Attachment", safe(message.getAttachments())
                .stream().map(EmailAttachment::getFilename).toList());
    }

    private String renderHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return templateEngine.process("email/" + templateName, context);
    }

    private void logAttachments(String label, List<String> names) {
        if (!names.isEmpty()) {
            log.info("  {} {}: {}", label, names.size() == 1 ? "" : "×" + names.size(), names);
        }
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }
}