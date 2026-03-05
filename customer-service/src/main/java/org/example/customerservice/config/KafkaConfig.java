package org.example.customerservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.customerservice.dto.CustomerEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ReactiveKafkaProducerTemplate<String, CustomerEventDto> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        JsonSerializer<CustomerEventDto> valueSerializer = new JsonSerializer<>();
        valueSerializer.setAddTypeInfo(false);
        
        SenderOptions<String, CustomerEventDto> senderOptions = 
                SenderOptions.<String, CustomerEventDto>create(props)
                        .withValueSerializer(valueSerializer)
                        .maxInFlight(1024);
        
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}

