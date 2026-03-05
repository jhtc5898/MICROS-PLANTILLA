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
public class CustomerEventDto {
    
    @JsonProperty("event_type")
    private String eventType; // CREATED, UPDATED, DELETED
    
    @JsonProperty("customer_id")
    private Long customerId;
    
    @JsonProperty("person_id")
    private Long personId;
    
    @JsonProperty("timestamp")
    private Long timestamp;
}

