package com.example.captcha.service;

import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

@Service
public class CaptchaService {
    
    private static final int WIDTH = 200;
    private static final int HEIGHT = 50;
    private static final int CAPTCHA_LENGTH = 6;
    private final Random random = new Random();
    
    public record CaptchaData(String image, String text) {}
    
    public CaptchaData generateCaptcha() {
        // Generate random text
        String captchaText = generateRandomText();
        
        // Create the image
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Add noise (random lines)
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            g2d.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        // Set text properties
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textX = (WIDTH - fontMetrics.stringWidth(captchaText)) / 2;
        int textY = (HEIGHT + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
        
        // Add slight rotation to each character
        char[] chars = captchaText.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            double rotation = -20 + random.nextInt(40);
            g2d.rotate(Math.toRadians(rotation), textX + (i * 20), textY);
            g2d.drawString(String.valueOf(chars[i]), textX + (i * 20), textY);
            g2d.rotate(Math.toRadians(-rotation), textX + (i * 20), textY);
        }
        
        // Create the CAPTCHA (Draw the text on the image)
        g2d.dispose();
        
        // Convert image to Base64
        String base64Image = imageToBase64(image);
        
        return new CaptchaData(base64Image, captchaText);
    }
    
    private String generateRandomText() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captchaText.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captchaText.toString();
    }
    
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error converting image to Base64", e);
        }
    }
}
