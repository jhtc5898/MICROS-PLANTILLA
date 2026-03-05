package org.example.accountservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient setup.
 * Configures WebClient beans for external service communication.
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    @Value("${customer.service.url:http://localhost:8080}")
    private String customerServiceUrl;
    
    /**
     * Creates a WebClient bean for communicating with the Customer Service.
     * Sets the base URL for the Customer Service API.
     *
     * @return WebClient configured for Customer Service
     */
    @Bean
    public WebClient customerServiceWebClient() {
        log.info("Configuring WebClient for Customer Service with URL: {}", customerServiceUrl);
        return WebClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }
}

