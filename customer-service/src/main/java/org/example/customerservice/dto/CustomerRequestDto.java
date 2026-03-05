package org.example.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDto {
    
    @Valid
    @NotNull(message = "Person data is required")
    @JsonProperty("person")
    private PersonDto person;
    
    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    private String password;
    
    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private Boolean status;
}

