package org.example.customerservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad que representa la información personal de una persona en el sistema.
 * Contiene datos básicos como nombre, género, identificación, dirección y teléfono.
 * Esta entidad se usa en conjunto con Customer para formar un cliente completo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("persons")
public class Person {
    
    /** ID único de la persona, generado automáticamente. */
    @Id
    private Long id;
    
    /** Nombre de la persona. */
    @Column("name")
    private String name;
    
    /** Género de la persona. */
    @Column("gender")
    private String gender;
    
    /** Identificación única de la persona. */
    @Column("identification")
    private String identification;
    
    /** Dirección de la persona. */
    @Column("address")
    private String address;
    
    /** Número de teléfono de la persona. */
    @Column("phone")
    private String phone;
    
    /** Fecha y hora de creación del registro. */
    @Column("created_at")
    private LocalDateTime createdAt;
    
    /** Fecha y hora de la última actualización del registro. */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

