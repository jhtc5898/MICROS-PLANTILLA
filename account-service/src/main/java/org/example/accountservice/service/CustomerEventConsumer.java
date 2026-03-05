package org.example.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.dto.CustomerEventDto;
import org.example.accountservice.entity.Customer;
import org.example.accountservice.repository.CustomerRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventConsumer {
    
    private final ReceiverOptions<String, CustomerEventDto> kafkaReceiverOptions;
    private final CustomerRepository customerRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    
    @PostConstruct
    @SuppressWarnings("null")
    public void consumeCustomerEvents() {
        KafkaReceiver.create(kafkaReceiverOptions)
                .receive()
                .subscribe(record -> {
                    CustomerEventDto event = record.value();
                    log.info("Received customer event: {} for customer ID: {}", 
                            event.getEventType(), event.getCustomerId());
                    
                    switch (event.getEventType()) {
                        case "CREATED":
                            log.info("Customer created: {}", event.getCustomerId());
                            Customer customer = Customer.builder().id(event.getCustomerId()).status("ACTIVE").build();
                            r2dbcEntityTemplate.insert(customer).subscribe();
                            break;
                        case "UPDATED":
                            log.info("Customer updated: {}", event.getCustomerId());
                            customerRepository.findById(event.getCustomerId())
                                .flatMap(c -> {
                                    c.setStatus("ACTIVE");
                                    return customerRepository.save(c);
                                })
                                .switchIfEmpty(r2dbcEntityTemplate.insert(Customer.builder().id(event.getCustomerId()).status("ACTIVE").build()).then(Mono.just(Customer.builder().id(event.getCustomerId()).status("ACTIVE").build())))
                                .subscribe();
                            break;
                        case "DELETED":
                            log.info("Customer deleted: {}", event.getCustomerId());
                            customerRepository.findById(event.getCustomerId())
                                .flatMap(c -> {
                                    c.setStatus("INACTIVE");
                                    return customerRepository.save(c);
                                })
                                .switchIfEmpty(r2dbcEntityTemplate.insert(Customer.builder().id(event.getCustomerId()).status("INACTIVE").build()).then(Mono.just(Customer.builder().id(event.getCustomerId()).status("INACTIVE").build())))
                                .subscribe();
                            break;
                        default:
                            log.warn("Unknown event type: {}", event.getEventType());
                    }
                    
                    record.receiverOffset().acknowledge();
                });
    }
}

