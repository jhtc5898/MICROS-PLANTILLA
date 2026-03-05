package org.example.accountservice.mapper;

import org.example.accountservice.dto.MovementDto;
import org.example.accountservice.entity.Movement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MovementMapper {
    
    MovementMapper INSTANCE = Mappers.getMapper(MovementMapper.class);
    
    MovementDto toDto(Movement movement);
    
    Movement toEntity(MovementDto movementDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(MovementDto movementDto, @MappingTarget Movement movement);
}

