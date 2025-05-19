package com.assignment_two_starter.controller;

import com.assignment_two_starter.service.CustomerService;
import com.assignment_two_starter.service.QRCodeService;
import com.google.zxing.WriterException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final CustomerService customerService;

    public QRCodeController(QRCodeService qrCodeService, CustomerService customerService) {
        this.qrCodeService = qrCodeService;
        this.customerService = customerService;
    }

    /**
     * Generates a QR code image for Google Authenticator.
     *
     * @param email The email of the user.
     * @return A PNG image of the QR code.
     */
    @GetMapping(value = "/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(@RequestParam String email) throws WriterException, IOException {
        String secretKey = customerService.generateSecretKey(email);
        String qrCodeUrl = customerService.generateQrCodeUrl(email, secretKey);

        byte[] qrCodeImage = qrCodeService.generateQRCodeImage(qrCodeUrl, 200, 200);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);
    }
}
