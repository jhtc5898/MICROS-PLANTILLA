package org.example.accountservice.mapper;

import org.example.accountservice.dto.AccountDto;
import org.example.accountservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);
    
    AccountDto toDto(Account account);
    
    Account toEntity(AccountDto accountDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AccountDto accountDto, @MappingTarget Account account);
}

