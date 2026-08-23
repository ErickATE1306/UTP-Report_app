package com.utp_reporta_backend.modules.notificacion.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateVariables) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        Context context = new Context();
        context.setVariables(templateVariables);

        String htmlContent = templateEngine.process("email/" + templateName, context);

        helper.setFrom("noreply@utpreporta.com"); // Replace with your actual email
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // Set to true for HTML content

        mailSender.send(mimeMessage);
    }
}

