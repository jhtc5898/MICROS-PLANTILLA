package org.example.customerservice.repository;

import org.example.customerservice.entity.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio para la entidad Customer, proporciona operaciones CRUD reactivas y consultas personalizadas.
 * Extiende ReactiveCrudRepository para operaciones básicas y define métodos adicionales con consultas SQL.
 */
@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {
    
    /**
     * Busca un cliente por la identificación de la persona asociada.
     * Realiza un JOIN con la tabla persons para filtrar por identificación.
     * @param identification La identificación de la persona.
     * @return Un Mono con el cliente encontrado, o vacío si no existe.
     */
    @Query("SELECT c.id, c.person_id, c.password, c.status, c.created_at, c.updated_at FROM customers c INNER JOIN persons p ON c.person_id = p.id WHERE p.identification = :identification")
    Mono<Customer> findByIdentification(String identification);
    
    /**
     * Busca todos los clientes con un estado específico.
     * @param status El estado del cliente (true para activo, false para inactivo).
     * @return Un Flux con los clientes que coinciden con el estado.
     */
    @Query("SELECT c.id, c.person_id, c.password, c.status, c.created_at, c.updated_at FROM customers c WHERE c.status = :status")
    Flux<Customer> findByStatus(Boolean status);
    
    /**
     * Busca un cliente por el ID de la persona asociada.
     * @param personId El ID de la persona.
     * @return Un Mono con el cliente encontrado, o vacío si no existe.
     */
    @Query("SELECT c.id, c.person_id, c.password, c.status, c.created_at, c.updated_at FROM customers c WHERE c.person_id = :personId")
    Mono<Customer> findByPersonId(Long personId);
}

