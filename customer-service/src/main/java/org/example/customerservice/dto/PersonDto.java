package org.example.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {
    
    private Long id;
    
    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "Gender is required")
    @JsonProperty("gender")
    private String gender;
    
    @NotBlank(message = "Identification is required")
    @JsonProperty("identification")
    private String identification;
    
    @NotBlank(message = "Address is required")
    @JsonProperty("address")
    private String address;
    
    @NotBlank(message = "Phone is required")
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

