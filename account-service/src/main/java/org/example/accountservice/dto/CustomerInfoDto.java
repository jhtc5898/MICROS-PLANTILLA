package org.example.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDto {
    
    @JsonProperty("id")
    private Long id;
    
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
}

