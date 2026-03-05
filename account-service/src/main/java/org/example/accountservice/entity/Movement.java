package org.example.accountservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a bank movement.
 * Stores information about transactions including type, value, and balance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("movements")
public class Movement {
    
    @Id
    private Long id;
    
    @Column("account_id")
    private Long accountId;
    
    @Column("movement_date")
    private LocalDateTime movementDate;
    
    @Column("movement_type")
    private String movementType;
    
    @Column("value")
    private BigDecimal value;
    
    @Column("balance")
    private BigDecimal balance;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}

