package com.example.captcha.controller;

import com.example.captcha.service.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CaptchaControllerTest {

    @Mock
    private CaptchaService captchaService;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private CaptchaController captchaController;

    private MockHttpSession session;
    private static final String TEST_CAPTCHA_TEXT = "ABC123";
    private static final String TEST_IMAGE_DATA = "base64ImageData";
    private CaptchaService.CaptchaData mockCaptchaData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
        mockCaptchaData = new CaptchaService.CaptchaData(TEST_IMAGE_DATA, TEST_CAPTCHA_TEXT);
        
        // Setup default mock behavior
        when(captchaService.generateCaptcha())
                .thenReturn(mockCaptchaData);
    }

    @Test
    void generateCaptcha_Normal_Mode() {

        // Arrange
        when(bucket.tryConsume(1)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = captchaController.generateCaptcha(session, null);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(TEST_IMAGE_DATA, response.getBody().get("image"));
        assertNull(response.getBody().get("captchaText")); // Should not contain text in normal mode
        assertEquals(TEST_CAPTCHA_TEXT, session.getAttribute("captchaText")); // Should store in session
        
        // Verify the service was called
        verify(captchaService, times(1)).generateCaptcha();
    }

    @Test
    void generateCaptcha_Test_Mode() {

        // Arrange
        when(bucket.tryConsume(1)).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, String>> response = captchaController.generateCaptcha(session, "true");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(TEST_IMAGE_DATA, response.getBody().get("image"));
        assertEquals(TEST_CAPTCHA_TEXT, response.getBody().get("captchaText")); // Should contain text in test mode
        assertEquals("true", response.getBody().get("testMode"));
        assertEquals(TEST_CAPTCHA_TEXT, session.getAttribute("captchaText")); // Should store in session
        
        // Verify the service was called
        verify(captchaService, times(1)).generateCaptcha();
    }

    @Test
    void verifyCaptcha_Valid_Input() {
        // Arrange
        session.setAttribute("captchaText", TEST_CAPTCHA_TEXT);
        Map<String, String> request = Map.of("captcha", TEST_CAPTCHA_TEXT);

        // Act
        ResponseEntity<Map<String, Object>> response = captchaController.verifyCaptcha(request, null, session);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("valid"));
        assertEquals("CAPTCHA verified successfully!", response.getBody().get("message"));
        assertNull(session.getAttribute("captchaText")); // Should clear session after verification
    }

    @Test
    void verifyCaptcha_Invalid_Input() {
        // Arrange
        String userInput = "XYZ789";
        session.setAttribute("captchaText", TEST_CAPTCHA_TEXT);
        Map<String, String> request = Map.of("captcha", userInput);

        // Act
        ResponseEntity<Map<String, Object>> response = captchaController.verifyCaptcha(request, null, session);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("valid"));
        assertEquals("Invalid CAPTCHA text. Please try again.", response.getBody().get("message"));
        assertNull(session.getAttribute("captchaText")); // Should clear session after verification
    }

    @Test
    void verifyCaptcha_Test_Mode() {
        // Arrange
        String userInput = "XYZ789";
        session.setAttribute("captchaText", TEST_CAPTCHA_TEXT);
        Map<String, String> request = Map.of("captcha", userInput);

        // Act
        ResponseEntity<Map<String, Object>> response = captchaController.verifyCaptcha(request, "true", session);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("valid"));
        assertEquals("Invalid CAPTCHA text. Please try again.", response.getBody().get("message"));
        assertTrue((Boolean) response.getBody().get("testMode"));
        assertEquals(userInput, response.getBody().get("userInput"));
        assertEquals(TEST_CAPTCHA_TEXT, response.getBody().get("expectedCaptcha"));
    }

    @Test
    void verifyCaptcha_No_Session_Captcha() {
        // Arrange
        Map<String, String> request = Map.of("captcha", TEST_CAPTCHA_TEXT);

        // Act
        ResponseEntity<Map<String, Object>> response = captchaController.verifyCaptcha(request, null, session);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("valid"));
        assertEquals("Invalid CAPTCHA text. Please try again.", response.getBody().get("message"));
    }

    @Test
    void generateCaptcha_RateLimitExceeded() {
        // Arrange
        when(bucket.tryConsume(1)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = captchaController.generateCaptcha(session, null);

        // Assert
        assertEquals(429, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Too many requests. Please try again later after 1 minute.", response.getBody().get("error"));
    }

    @Test
    void generateCaptcha_MultipleRequests_RateLimitExceeded() {
        // Arrange
        when(bucket.tryConsume(1)).thenReturn(true, true, true, true, true, false);

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            ResponseEntity<Map<String, String>> response = captchaController.generateCaptcha(session, null);
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }

        // The 6th request should be rate limited
        ResponseEntity<Map<String, String>> response = captchaController.generateCaptcha(session, null);
        assertEquals(429, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Too many requests. Please try again later after 1 minute.", response.getBody().get("error"));
    }
}
