package org.example.accountservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

/**
 * Entity representing a customer in the account service.
 * Stores customer ID and status for account management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customers")
public class Customer {
    
    @Id
    @NonNull
    private Long id;
    
    @Column("status")
    @NonNull
    private String status;
}
