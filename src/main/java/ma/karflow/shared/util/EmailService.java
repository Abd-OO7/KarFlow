package ma.karflow.shared.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service d'envoi d'emails transactionnels avec templates Thymeleaf.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${karflow.mail.from}")
    private String fromAddress;

    /**
     * Envoie un email HTML basé sur un template Thymeleaf.
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email envoyé à {} — sujet: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {} — sujet: {}", to, subject, e);
        }
    }

    /**
     * Envoie un email HTML avec une pièce jointe PDF.
     */
    @Async
    public void sendHtmlEmailWithPdf(String to, String subject, String templateName,
                                     Map<String, Object> variables, byte[] pdfContent,
                                     String pdfFileName) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addAttachment(pdfFileName, new ByteArrayResource(pdfContent), "application/pdf");

            mailSender.send(message);
            log.info("Email avec PDF envoyé à {} — sujet: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec PDF à {} — sujet: {}", to, subject, e);
        }
    }
}
