package com.ednevnik.dnevnik.mapper;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            null, // Don't include password in DTO
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getPhoneNumber()
        );
    }

    public User toEntity(UserDto dto) {
        User user = new User();
        updateEntityFromDto(user, dto);
        return user;
    }

    public void updateEntityFromDto(User user, UserDto dto) {
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // Only set password if it's provided (not empty)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(dto.getPassword());
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
    }
} 