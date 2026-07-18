package com.charles.schwab.url.shortner.core.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    void testGenerateQrCode() {
        String testUrl = "http://localhost:8080/test";
        byte[] qrCodeBytes = qrCodeService.generateQrCode(testUrl);
        
        assertNotNull(qrCodeBytes);
        assertTrue(qrCodeBytes.length > 0);
        
        // PNG magic number validation (first 8 bytes: 89 50 4E 47 0D 0A 1A 0A)
        assertEquals((byte) 0x89, qrCodeBytes[0]);
        assertEquals((byte) 0x50, qrCodeBytes[1]);
        assertEquals((byte) 0x4E, qrCodeBytes[2]);
        assertEquals((byte) 0x47, qrCodeBytes[3]);
    }
}
