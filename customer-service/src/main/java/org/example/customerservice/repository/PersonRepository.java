package org.example.customerservice.repository;

import org.example.customerservice.entity.Person;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repositorio para la entidad Person, proporciona operaciones CRUD reactivas y consultas personalizadas.
 * Extiende ReactiveCrudRepository para operaciones básicas y define métodos adicionales con consultas SQL.
 */
@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {
    
    /**
     * Busca una persona por su identificación.
     * @param identification La identificación única de la persona.
     * @return Un Mono con la persona encontrada, o vacío si no existe.
     */
    @Query("SELECT id, name, gender, identification, address, phone, created_at, updated_at FROM persons WHERE identification = :identification")
    Mono<Person> findByIdentification(String identification);
    
    /**
     * Busca una persona por su ID.
     * Sobrescribe el método por defecto para usar una consulta personalizada.
     * @param id El ID de la persona.
     * @return Un Mono con la persona encontrada, o vacío si no existe.
     */
    @Query("SELECT id, name, gender, identification, address, phone, created_at, updated_at FROM persons WHERE id = :id")
    Mono<Person> findById(Long id);
}

