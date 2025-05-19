package com.assignment_two_starter.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {


    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an HTML email.
     *
     * @param to           Recipient email address.
     * @param subject      Email subject.
     * @param htmlContent  Full HTML content of the email.
     * @param qrCodeBase64 (Optional) QR code image encoded in Base64.
     *                     If provided, it will be appended to the HTML content.
     * @throws MessagingException if sending fails.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent, String qrCodeBase64) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("babynestemail@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            htmlContent += "<br><br><img src='data:image/png;base64," + qrCodeBase64 + "' alt='QR Code' />";
        }

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    /**
     * Sends an HTML email without any QR code.
     *
     * @param to          Recipient email address.
     * @param subject     Email subject.
     * @param htmlContent Full HTML content of the email.
     * @throws MessagingException if sending fails.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        sendHtmlEmail(to, subject, htmlContent, "");
    }

}
