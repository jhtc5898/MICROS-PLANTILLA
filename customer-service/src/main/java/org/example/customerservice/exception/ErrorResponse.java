package org.example.customerservice.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para representar respuestas de error estandarizadas en la API.
 * Contiene información como timestamp, código de estado, mensaje y detalles adicionales.
 * Utiliza Lombok para generar getters y setters automáticamente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /** Timestamp cuando ocurrió el error. */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    /** Código de estado HTTP del error. */
    @JsonProperty("status")
    private Integer status;
    
    /** Tipo de error (ej. "Bad Request"). */
    @JsonProperty("error")
    private String error;
    
    /** Mensaje descriptivo del error. */
    @JsonProperty("message")
    private String message;
    
    /** Ruta de la solicitud que causó el error. */
    @JsonProperty("path")
    private String path;
    
    /** Mapa de errores de validación, si aplica (campo -> mensaje). */
    @JsonProperty("errors")
    private Map<String, String> errors;
}

