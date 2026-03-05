package org.example.customerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CustomerDto;
import org.example.customerservice.dto.CustomerRequestDto;
import org.example.customerservice.dto.CustomerUpdateDto;
import org.example.customerservice.dto.PersonDto;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.entity.Person;
import org.example.customerservice.mapper.PersonMapper;
import org.example.customerservice.repository.CustomerRepository;
import org.example.customerservice.repository.PersonRepository;
import org.example.customerservice.dto.CustomerEventDto;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Servicio para la gestión de clientes, maneja operaciones CRUD y lógica de negocio.
 * Integra con PersonService y envía eventos a Kafka para comunicación asíncrona.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final ReactiveKafkaProducerTemplate<String, CustomerEventDto> kafkaProducerTemplate;
    
    /**
     * Crea un nuevo cliente con la información de persona proporcionada.
     * Verifica si la persona ya existe, si no, la crea. Luego crea el cliente y envía evento a Kafka.
     * @param requestDto Los datos del cliente y persona a crear.
     * @return Un Mono con el cliente creado.
     */
    @Transactional
    public Mono<CustomerDto> createCustomer(CustomerRequestDto requestDto) {
        log.info("Creating customer for person with identification: {}", requestDto.getPerson().getIdentification());
        
        return personRepository.findByIdentification(requestDto.getPerson().getIdentification())
                .flatMap(existingPerson -> 
                    customerRepository.findByPersonId(existingPerson.getId())
                            .flatMap(existingCustomer -> 
                                Mono.error(new RuntimeException(
                                    String.format("Ya existe un cliente registrado con la identificación '%s'. Por favor, utilice una identificación diferente.", 
                                    requestDto.getPerson().getIdentification()))))
                            .cast(Customer.class)
                            .switchIfEmpty(
                                    Mono.defer(() -> {
                                        existingPerson.setName(requestDto.getPerson().getName());
                                        existingPerson.setGender(requestDto.getPerson().getGender());
                                        existingPerson.setAddress(requestDto.getPerson().getAddress());
                                        existingPerson.setPhone(requestDto.getPerson().getPhone());
                                        existingPerson.setUpdatedAt(LocalDateTime.now());
                                        
                                        return personRepository.save(existingPerson)
                                                .flatMap(updatedPerson -> {
                                                    Customer customer = Customer.builder()
                                                            .personId(updatedPerson.getId())
                                                            .password(requestDto.getPassword())
                                                            .status(requestDto.getStatus())
                                                            .createdAt(LocalDateTime.now())
                                                            .updatedAt(LocalDateTime.now())
                                                            .build();
                                                    return customerRepository.save(customer);
                                                });
                                    })
                            )
                )
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Person person = personMapper.toEntity(
                                    PersonDto.builder()
                                            .name(requestDto.getPerson().getName())
                                            .gender(requestDto.getPerson().getGender())
                                            .identification(requestDto.getPerson().getIdentification())
                                            .address(requestDto.getPerson().getAddress())
                                            .phone(requestDto.getPerson().getPhone())
                                            .build()
                            );
                            person.setCreatedAt(LocalDateTime.now());
                            person.setUpdatedAt(LocalDateTime.now());
                            
                            return personRepository.save(person)
                                    .flatMap(savedPerson -> {
                                        Customer customer = Customer.builder()
                                                .personId(savedPerson.getId())
                                                .password(requestDto.getPassword())
                                                .status(requestDto.getStatus())
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build();
                                        return customerRepository.save(customer);
                                    });
                        })
                )
                .doOnSuccess(savedCustomer -> 
                    sendCustomerEvent("CREATED", savedCustomer.getId(), savedCustomer.getPersonId())
                            .subscribe(
                                    null,
                                    error -> log.error("Error sending customer event (non-blocking): {}", error.getMessage())
                            )
                )
                .flatMap(customer -> 
                    personRepository.findById(customer.getPersonId())
                            .map(person -> CustomerDto.builder()
                                    .id(customer.getId())
                                    .name(person.getName())
                                    .gender(person.getGender())
                                    .identification(person.getIdentification())
                                    .address(person.getAddress())
                                    .phone(person.getPhone())
                                    .password(customer.getPassword())
                                    .status(customer.getStatus())
                                    .createdAt(customer.getCreatedAt())
                                    .updatedAt(customer.getUpdatedAt())
                                    .build())
                )
                .doOnSuccess(c -> log.info("Customer created successfully with ID: {}", c.getId()))
                .doOnError(error -> log.error("Error creating customer: {}", error.getMessage()));
    }
    
    /**
     * Obtiene un cliente por su ID, incluyendo la información de la persona asociada.
     * @param id El ID del cliente.
     * @return Un Mono con el cliente encontrado, o error si no existe.
     */
    public Mono<CustomerDto> getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found with ID: " + id)))
                .flatMap(customer -> 
                    personRepository.findById(customer.getPersonId())
                            .map(person -> buildCustomerDto(customer, person))
                )
                .doOnError(error -> log.error("Error fetching customer: {}", error.getMessage()));
    }
    
    /**
     * Obtiene todos los clientes activos (status = true), incluyendo la información de persona.
     * @return Un Flux con la lista de clientes activos.
     */
    public Flux<CustomerDto> getAllCustomers() {
        log.info("Fetching all active customers (status = true)");
        return customerRepository.findByStatus(true)
                .flatMap(customer -> 
                    personRepository.findById(customer.getPersonId())
                            .map(person -> buildCustomerDto(customer, person))
                            .switchIfEmpty(Mono.just(buildCustomerDtoWithoutPerson(customer)))
                )
                .doOnError(error -> log.error("Error fetching customers: {}", error.getMessage()));
    }
    
    /**
     * Actualiza un cliente existente con los nuevos datos proporcionados.
     * Permite actualizar persona y cliente, luego envía evento a Kafka.
     * @param id El ID del cliente a actualizar.
     * @param updateDto Los datos a actualizar.
     * @return Un Mono con el cliente actualizado.
     */
    @Transactional
    public Mono<CustomerDto> updateCustomer(Long id, CustomerUpdateDto updateDto) {
        log.info("Updating customer with ID: {}", id);
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found with ID: " + id)))
                .flatMap(existingCustomer -> {
                    if (updateDto.getPassword() != null) {
                        existingCustomer.setPassword(updateDto.getPassword());
                    }
                    if (updateDto.getStatus() != null) {
                        existingCustomer.setStatus(updateDto.getStatus());
                    }
                    existingCustomer.setUpdatedAt(LocalDateTime.now());
                    
                    return personRepository.findById(existingCustomer.getPersonId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Person not found for customer")))
                            .flatMap(existingPerson -> {
                                if (updateDto.getName() != null) {
                                    existingPerson.setName(updateDto.getName());
                                }
                                if (updateDto.getGender() != null) {
                                    existingPerson.setGender(updateDto.getGender());
                                }
                                if (updateDto.getIdentification() != null) {
                                    existingPerson.setIdentification(updateDto.getIdentification());
                                }
                                if (updateDto.getAddress() != null) {
                                    existingPerson.setAddress(updateDto.getAddress());
                                }
                                if (updateDto.getPhone() != null) {
                                    existingPerson.setPhone(updateDto.getPhone());
                                }
                                existingPerson.setUpdatedAt(LocalDateTime.now());
                                
                                return personRepository.save(existingPerson)
                                        .then(customerRepository.save(existingCustomer))
                                        .flatMap(updated -> {
                                            sendCustomerEvent("UPDATED", updated.getId(), updated.getPersonId())
                                                    .subscribe(
                                                            null,
                                                            error -> log.error("Error sending customer event (non-blocking): {}", error.getMessage())
                                                    );
                                            return personRepository.findById(updated.getPersonId())
                                                    .map(person -> buildCustomerDto(updated, person));
                                        });
                            });
                })
                .doOnSuccess(c -> log.info("Customer updated successfully with ID: {}", c.getId()))
                .doOnError(error -> log.error("Error updating customer: {}", error.getMessage()));
    }
    
    /**
     * Desactiva (eliminación lógica) un cliente cambiando su status a false.
     * Envía evento a Kafka indicando la desactivación.
     * @param id El ID del cliente a desactivar.
     * @return Un Mono vacío cuando se complete.
     */
    @Transactional
    public Mono<Void> deleteCustomer(Long id) {
        log.info("Deactivating customer with ID: {}", id);
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found with ID: " + id)))
                .flatMap(customer -> {
                    customer.setStatus(false);
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer)
                            .doOnSuccess(saved -> 
                                sendCustomerEvent("DELETED", saved.getId(), saved.getPersonId())
                                        .subscribe(
                                                null,
                                                error -> log.error("Error sending customer event (non-blocking): {}", error.getMessage())
                                        )
                            )
                            .then();
                })
                .doOnSuccess(v -> log.info("Customer deactivated successfully with ID: {}", id))
                .doOnError(error -> log.error("Error deactivating customer: {}", error.getMessage()));
    }
    
    /**
     * Envía un evento de cliente a Kafka de forma asíncrona.
     * @param eventType El tipo de evento (CREATED, UPDATED, DELETED).
     * @param customerId El ID del cliente.
     * @param personId El ID de la persona.
     * @return Un Mono vacío.
     */
    private Mono<Void> sendCustomerEvent(String eventType, Long customerId, Long personId) {
        log.info("Sending customer event: {} for customer ID: {}", eventType, customerId);
        return kafkaProducerTemplate.send("customer-events", 
                CustomerEventDto.builder()
                        .eventType(eventType)
                        .customerId(customerId)
                        .personId(personId)
                        .timestamp(System.currentTimeMillis())
                        .build())
                .then()
                .timeout(java.time.Duration.ofSeconds(5))
                .onErrorResume(error -> {
                    log.error("Error sending customer event (will not block operation): {}", error.getMessage());
                    return Mono.empty(); // Return empty to not block
                });
    }
    
    /**
     * Construye un CustomerDto combinando datos de Customer y Person.
     * @param customer La entidad Customer.
     * @param person La entidad Person asociada.
     * @return El CustomerDto completo.
     */
    private CustomerDto buildCustomerDto(Customer customer, Person person) {
        return CustomerDto.builder()
                .id(customer.getId())
                .name(person.getName())
                .gender(person.getGender())
                .identification(person.getIdentification())
                .address(person.getAddress())
                .phone(person.getPhone())
                .password(customer.getPassword())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
    
    /**
     * Construye un CustomerDto solo con datos de Customer, sin información de persona.
     * Usado cuando no se puede obtener la persona.
     * @param customer La entidad Customer.
     * @return El CustomerDto parcial.
     */
    private CustomerDto buildCustomerDtoWithoutPerson(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .name(null)
                .gender(null)
                .identification(null)
                .address(null)
                .phone(null)
                .password(customer.getPassword())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}

