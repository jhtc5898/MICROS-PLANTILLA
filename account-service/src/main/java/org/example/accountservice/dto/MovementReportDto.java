package org.example.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementReportDto {
    
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    
    @JsonProperty("cliente")
    private String cliente;
    
    @JsonProperty("numero_cuenta")
    private String numeroCuenta;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("saldo_inicial")
    private BigDecimal saldoInicial;
    
    @JsonProperty("estado")
    private Boolean estado;
    
    @JsonProperty("valor_movimiento")
    private BigDecimal valorMovimiento;
    
    @JsonProperty("tipo_movimiento")
    private String tipoMovimiento; // Crédito o Débito
    
    @JsonProperty("saldo_disponible")
    private BigDecimal saldoDisponible;
}

