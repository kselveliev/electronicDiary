package com.ednevnik.dnevnik.mapper;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {

    private SchoolRepository schoolRepository;

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setNationalId(user.getNationalId());
        dto.setSchoolId(user.getSchool() != null ? user.getSchool().getId() : null);

        return dto;
    }

    public User toEntity(UserDto dto) {
        if (dto.getRole() == null) {
            throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }
        User user;
        switch (dto.getRole()) {
            case ROLE_TEACHER:
                user = new Teacher();
                break;
            case ROLE_STUDENT:
                user = new Student();
                break;
            case ROLE_PARENT:
                user = new Parent();
                break;
            case ROLE_DIRECTOR:
                user = new Director();
                break;
            case ROLE_ADMIN:
                user = new Admin();
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setNationalId(dto.getNationalId());

        // Set school if provided and not admin
        if (dto.getSchoolId() != null && dto.getRole() != UserRole.ROLE_ADMIN) {
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));
            user.setSchool(school);
        }

        return user;
    }

    public void updateEntityFromDto(User user, UserDto dto) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(dto.getPassword());
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setNationalId(dto.getNationalId());

        // Update school if provided and not admin
        if (dto.getSchoolId() != null && dto.getRole() != UserRole.ROLE_ADMIN) {
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));
            user.setSchool(school);
        } else if (dto.getRole() == UserRole.ROLE_ADMIN) {
            user.setSchool(null);
        }
    }
} 