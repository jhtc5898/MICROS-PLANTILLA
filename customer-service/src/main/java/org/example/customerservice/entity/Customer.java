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
 * Entidad que representa un cliente en el sistema de microservicios.
 * Un cliente está ligado a una persona (Person) mediante person_id y añade campos específicos como contraseña y estado de actividad.
 * Se utiliza para autenticación y gestión de cuentas de usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customers")
public class Customer {
    
    /** ID único del cliente, generado automáticamente. */
    @Id
    private Long id;
    
    /** ID de la persona asociada al cliente. */
    @Column("person_id")
    private Long personId;
    
    /** Contraseña del cliente para autenticación. */
    @Column("password")
    private String password;
    
    /** Estado del cliente (true para activo, false para inactivo). */
    @Column("status")
    private Boolean status;
    
    /** Fecha y hora de creación del registro. */
    @Column("created_at")
    private LocalDateTime createdAt;
    
    /** Fecha y hora de la última actualización del registro. */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

