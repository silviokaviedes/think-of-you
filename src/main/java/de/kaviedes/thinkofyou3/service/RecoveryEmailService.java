package de.kaviedes.thinkofyou3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RecoveryEmailService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public RecoveryEmailService(JavaMailSender mailSender,
                                @Value("${app.recovery.mail.enabled:false}") boolean enabled,
                                @Value("${app.recovery.mail.from:noreply@think-of-you.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public void sendRecoveryCode(String toAddress, String username, String recoveryCode) {
        String normalizedAddress = normalizeEmail(toAddress);
        if (!enabled) {
            throw new RuntimeException("Recovery email delivery is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(normalizedAddress);
        message.setSubject("Your Thinking of You recovery code");
        message.setText("""
                A recovery code was requested for your Thinking of You account.

                Username: %s
                Recovery code: %s

                Keep this code private. The app does not store this email address.
                """.formatted(username, recoveryCode));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new RuntimeException("Failed to send recovery email");
        }
    }

    public boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }

    public String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Recovery email is required");
        }
        String normalized = email.trim();
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new RuntimeException("Recovery email is invalid");
        }
        return normalized;
    }
}
