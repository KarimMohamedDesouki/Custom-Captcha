package com.example.captcha.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    @Bean
    public Bucket createBucket() {
        Refill refill = Refill.intervally(5, Duration.ofMinutes(1)); // 5 tokens per minute
        Bandwidth limit = Bandwidth.classic(5, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
