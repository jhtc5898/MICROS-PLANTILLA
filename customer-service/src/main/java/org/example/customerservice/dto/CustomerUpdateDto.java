package org.example.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateDto {
    
    // Solo el ID es obligatorio, el resto son opcionales
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("identification")
    private String identification;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("status")
    private Boolean status;
}

