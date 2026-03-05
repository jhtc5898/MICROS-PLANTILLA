package org.example.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CustomerDto - Customer inherits from Person, so all Person fields are included
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    
    private Long id;
    
    // Inherited from Person
    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "Gender is required")
    @JsonProperty("gender")
    private String gender;
    
    @NotBlank(message = "Identification is required")
    @JsonProperty("identification")
    private String identification;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("phone")
    private String phone;
    
    // Customer specific fields
    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    private String password;
    
    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private Boolean status;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

