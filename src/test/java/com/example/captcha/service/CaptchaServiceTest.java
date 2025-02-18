package com.example.captcha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaServiceTest {

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService();
    }

    @Test
    void generateCaptcha_ShouldReturnValidData() {
        // Act
        CaptchaService.CaptchaData result = captchaService.generateCaptcha();

        // Assert
        assertNotNull(result, "CaptchaData should not be null");
        assertNotNull(result.text(), "CAPTCHA text should not be null");
        assertNotNull(result.image(), "CAPTCHA image should not be null");
        assertTrue(result.text().length() > 0, "CAPTCHA text should not be empty");
        assertTrue(result.image().length() > 0, "CAPTCHA image should not be empty");
    }

    @Test
    void generateCaptcha_ShouldGenerateValidBase64Image() throws Exception {
        // Act
        CaptchaService.CaptchaData result = captchaService.generateCaptcha();

        // Assert
        // Verify it's valid Base64
        byte[] imageBytes = Base64.getDecoder().decode(result.image());
        assertNotNull(imageBytes, "Should decode to valid bytes");
        
        // Verify it's a valid image
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(image, "Should be a valid image");
        assertTrue(image.getWidth() > 0, "Image should have positive width");
        assertTrue(image.getHeight() > 0, "Image should have positive height");
    }

    @RepeatedTest(10)
    void generateCaptcha_ShouldGenerateUniqueValues() {
        // Arrange
        Set<String> captchaTexts = new HashSet<>();
        Set<String> captchaImages = new HashSet<>();

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            CaptchaService.CaptchaData result = captchaService.generateCaptcha();
            
            assertTrue(captchaTexts.add(result.text()), 
                "CAPTCHA text should be unique: " + result.text());
            assertTrue(captchaImages.add(result.image()), 
                "CAPTCHA image should be unique: " + result.image().substring(0, 20) + "...");
        }
    }

}
