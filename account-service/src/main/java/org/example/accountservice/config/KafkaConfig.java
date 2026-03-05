package org.example.accountservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.accountservice.dto.CustomerEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for setting up Kafka consumer properties and creating ReceiverOptions
 * for consuming customer event messages from Kafka.
 */
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    /**
     * Bean method that creates and configures ReceiverOptions for consuming CustomerEventDto messages
     * from the 'customer-events' topic using reactive Kafka.
     *
     * @return ReceiverOptions configured for the customer events topic
     */
    @Bean
    public ReceiverOptions<String, CustomerEventDto> kafkaReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        JsonDeserializer<CustomerEventDto> valueDeserializer = new JsonDeserializer<>(CustomerEventDto.class);
        valueDeserializer.setUseTypeHeaders(false);
        
        return ReceiverOptions.<String, CustomerEventDto>create(props)
                .withValueDeserializer(valueDeserializer)
                .subscription(List.of("customer-events"));
    }
}

