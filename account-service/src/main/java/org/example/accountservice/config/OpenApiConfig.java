package org.example.accountservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * Sets up the API metadata for the Account Service.
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * Creates a custom OpenAPI bean for API documentation.
     * Configures the title, version, and description for the Account Service API.
     *
     * @return OpenAPI configuration object
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .version("1.0.0")
                        .description("API for managing accounts and movements"));
    }
}

