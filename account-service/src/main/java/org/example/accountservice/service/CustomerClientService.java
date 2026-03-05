package org.example.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.CustomerInfoDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerClientService {
    
    private final WebClient customerServiceWebClient;
    
    public Mono<CustomerInfoDto> getCustomerById(Long customerId) {
        String uri = "/api/v1/customers/" + customerId;
        log.info("Fetching customer information for ID: {} from URI: {}", customerId, uri);
        return customerServiceWebClient
                .get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    log.error("Error response from customer service: Status {} for customer ID {}", response.statusCode(), customerId);
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> log.error("Error response body: {}", body))
                            .then(Mono.error(new RuntimeException("Customer service returned error: " + response.statusCode())));
                })
                .bodyToMono(CustomerInfoDto.class)
                .doOnSuccess(c -> {
                    if (c != null && c.getName() != null && !c.getName().isEmpty()) {
                        log.info("Customer information retrieved successfully for ID {}: {}", customerId, c.getName());
                    } else {
                        log.warn("Customer information retrieved but name is null or empty for ID: {}. Customer object: {}", customerId, c);
                    }
                })
                .doOnError(error -> log.error("Error fetching customer information for ID {}: {}", customerId, error.getMessage(), error))
                .onErrorResume(error -> {
                    log.error("Could not fetch customer information for ID {}: {}. Returning default.", customerId, error.getMessage(), error);
                    return Mono.just(CustomerInfoDto.builder()
                            .id(customerId)
                            .name("Cliente no disponible")
                            .build());
                });
    }
}

