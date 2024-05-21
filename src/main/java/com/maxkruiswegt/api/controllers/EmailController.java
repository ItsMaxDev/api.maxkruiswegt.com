package com.maxkruiswegt.api.controllers;

import com.maxkruiswegt.api.models.dto.ContactForm;
import com.maxkruiswegt.api.services.EmailService;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EmailController {
    private final EmailService emailService;
    private final String contactEmail;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
        contactEmail = System.getProperty("RESEND_CONTACT_EMAIL");
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> sendContactForm(@RequestBody ContactForm contactForm) {
        // Sanitize the ContactForm details
        contactForm.setLocale(StringEscapeUtils.escapeHtml4(contactForm.getLocale().trim().toLowerCase()));
        contactForm.setName(StringEscapeUtils.escapeHtml4(contactForm.getName().trim()));
        contactForm.setEmail(StringEscapeUtils.escapeHtml4(contactForm.getEmail().trim().toLowerCase()));
        contactForm.setSubject(StringEscapeUtils.escapeHtml4(contactForm.getSubject().trim()));

        // Replace new lines with <br> and clean the message from any illegal HTML
        contactForm.setMessage(contactForm.getMessage().replace("\n", "<br>"));
        contactForm.setMessage(Jsoup.clean(contactForm.getMessage().trim(), Safelist.basic()));

        // Validate the ContactForm details
        if (contactForm.getLocale() == null || contactForm.getLocale().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Locale is required"));
        }
        if (contactForm.getName() == null || contactForm.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        // https://emailregex.com/index.html (RFC 5322 Official Standard)
        if (contactForm.getEmail() == null || contactForm.getEmail().isEmpty() || !contactForm.getEmail().matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email is required"));
        }
        if (contactForm.getSubject() == null || contactForm.getSubject().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subject is required"));
        }
        if (contactForm.getMessage() == null || contactForm.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        try {
            // Send the email to the specified contact email
            emailService.sendEmail(contactEmail, contactForm.getSubject(), String.format("Name: %s<br>Email: %s<br>Locale: %s<br><br><b>Message:</b><br>%s", contactForm.getName(), contactForm.getEmail(), contactForm.getLocale(), contactForm.getMessage()));

            // Send the email to the user to confirm the email was sent
            if (contactForm.getLocale().equals("nl")) {
                emailService.sendEmail(contactForm.getEmail(), "Email verzonden",
                        String.format("Hey %s,<br>Je bericht is ontvangen en ik zal er zo snel mogelijk naar kijken.<br><br>Met vriendelijke groet,<br>%s<br><br><a href=\"%s\">%s</a><br>%s<br><br><b>Bekijk hieronder je bericht:</b><br>%s",
                                contactForm.getName(), System.getProperty("RESEND_FROM_NAME"), "https://maxkruiswegt.com", "maxkruiswegt.com", "info@maxkruiswegt.com", contactForm.getMessage()));
                return ResponseEntity.ok(Map.of("message", "Email succesvol verzonden"));
            } else {
                emailService.sendEmail(contactForm.getEmail(), "Email sent",
                        String.format("Hey %s,<br>Your message has been received and I will look at it as soon as possible.<br><br>Kind regards,<br>%s<br><br><a href=\"%s\">%s</a><br>%s<br><br><b>Below is your message:</b><br>%s",
                                contactForm.getName(), System.getProperty("RESEND_FROM_NAME"), "https://maxkruiswegt.com", "maxkruiswegt.com", "info@maxkruiswegt.com", contactForm.getMessage()));
                return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to send email"));
        }
    }
}