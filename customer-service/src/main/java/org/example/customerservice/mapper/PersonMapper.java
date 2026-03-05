package org.example.customerservice.mapper;

import org.example.customerservice.dto.PersonDto;
import org.example.customerservice.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir entre entidades Person y DTOs PersonDto usando MapStruct.
 * Facilita la transformación de datos entre la capa de entidad y la capa de transferencia.
 */
@Mapper(componentModel = "spring")
public interface PersonMapper {
    
    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);
    
    /**
     * Convierte una entidad Person a un DTO PersonDto.
     * @param person La entidad Person a convertir.
     * @return El DTO PersonDto correspondiente.
     */
    PersonDto toDto(Person person);
    
    /**
     * Convierte un DTO PersonDto a una entidad Person.
     * @param personDto El DTO PersonDto a convertir.
     * @return La entidad Person correspondiente.
     */
    Person toEntity(PersonDto personDto);
    
    /**
     * Actualiza una entidad Person existente con los valores de un DTO PersonDto.
     * Ignora los campos id, createdAt y updatedAt para evitar sobrescribir valores sensibles.
     * @param personDto El DTO con los nuevos valores.
     * @param person La entidad Person a actualizar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(PersonDto personDto, @MappingTarget Person person);
}

