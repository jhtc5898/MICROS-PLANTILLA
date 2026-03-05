package org.example.customerservice.mapper;

import org.example.customerservice.dto.CustomerDto;
import org.example.customerservice.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir entre entidades Customer y DTOs CustomerDto usando MapStruct.
 * Facilita la transformación de datos entre la capa de entidad y la capa de transferencia.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    
    /**
     * Convierte una entidad Customer a un DTO CustomerDto.
     * @param customer La entidad Customer a convertir.
     * @return El DTO CustomerDto correspondiente.
     */
    CustomerDto toDto(Customer customer);
    
    /**
     * Convierte un DTO CustomerDto a una entidad Customer.
     * @param customerDto El DTO CustomerDto a convertir.
     * @return La entidad Customer correspondiente.
     */
    Customer toEntity(CustomerDto customerDto);
    
    /**
     * Actualiza una entidad Customer existente con los valores de un DTO CustomerDto.
     * Ignora los campos id, createdAt y updatedAt para evitar sobrescribir valores sensibles.
     * @param customerDto El DTO con los nuevos valores.
     * @param customer La entidad Customer a actualizar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CustomerDto customerDto, @MappingTarget Customer customer);
}

