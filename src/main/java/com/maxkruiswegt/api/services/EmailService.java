package com.maxkruiswegt.api.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    private final String fromEmail;
    private final String fromName;

    public EmailService() {
        if (System.getProperty("RESEND_API_KEY") == null || System.getProperty("RESEND_FROM_EMAIL") == null || System.getProperty("RESEND_FROM_NAME") == null || System.getProperty("RESEND_CONTACT_EMAIL") == null) {
            throw new NullPointerException("RESEND_API_KEY, RESEND_FROM_EMAIL, RESEND_FROM_NAME, and RESEND_CONTACT_EMAIL must be set in the environment");
        }

        this.resend = new Resend(System.getProperty("RESEND_API_KEY"));
        this.fromEmail = System.getProperty("RESEND_FROM_EMAIL");
        this.fromName = System.getProperty("RESEND_FROM_NAME");
    }

    public void sendEmail(String to, String subject, String html) throws ResendException {
        if (to == null || subject == null || html == null) {
            throw new NullPointerException("Email details cannot be null");
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmail + ">")
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        resend.emails().send(params);
    }
}
