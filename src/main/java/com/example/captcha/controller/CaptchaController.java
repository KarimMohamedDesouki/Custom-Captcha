package com.example.captcha.controller;

import io.github.bucket4j.Bucket;
import com.example.captcha.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;
    private final Bucket bucket;
    private final Logger logger = LoggerFactory.getLogger(CaptchaController.class);
    private static final String TEST_MODE_HEADER = "X-Captcha-Test-Mode";
    

    @Autowired
    public CaptchaController(CaptchaService captchaService, Bucket bucket) {
        this.captchaService = captchaService;
        this.bucket = bucket;
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateCaptcha(
            HttpSession session,
            @RequestHeader(value = TEST_MODE_HEADER, required = false) String testMode) {
        
        if (bucket.tryConsume(1)) {

        CaptchaService.CaptchaData captchaData = captchaService.generateCaptcha();
        
        // Store the CAPTCHA text in session
        session.setAttribute("captchaText", captchaData.text());
        logger.info("Generated new CAPTCHA. Text: {}", captchaData.text());
        
        Map<String, String> response = new HashMap<>();
        response.put("image", captchaData.image());
        
        // Only include the text in test mode
        if ("true".equalsIgnoreCase(testMode)) {
            response.put("testMode", "true");
            response.put("captchaText", captchaData.text());
            logger.info("Test mode enabled - returning CAPTCHA text in response");
        }
        
        return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(429).body(Map.of("error", "Too many requests. Please try again later after 1 minute."));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCaptcha(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = TEST_MODE_HEADER, required = false) String testMode,
            HttpSession session) {
        
        String userInput = request.get("captcha");
        String storedCaptcha = (String) session.getAttribute("captchaText");
        
        logger.info("Verifying CAPTCHA - User Input: {}, Stored CAPTCHA: {}", userInput, storedCaptcha);
        
        boolean isValid = storedCaptcha != null && 
                         storedCaptcha.equals(userInput);
        
        // Clear the session after verification
        session.removeAttribute("captchaText");
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "CAPTCHA verified successfully!" : "Invalid CAPTCHA text. Please try again.");
        
        // Include additional information in test mode
        if ("true".equalsIgnoreCase(testMode)) {
            response.put("testMode", true);
            response.put("userInput", userInput);
            response.put("expectedCaptcha", storedCaptcha);
        }
        
        logger.info("CAPTCHA verification result: {}, Message: {}", isValid, response.get("message"));
        
        return ResponseEntity.ok(response);
    }

}
