package com.ednevnik.dnevnik.mapper;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.*;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private UserMapper userMapper;

    private School testSchool;
    private Teacher testTeacher;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Test School");

        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUsername("testuser");
        testTeacher.setEmail("test@test.com");
        testTeacher.setPassword("password123");
        testTeacher.setFirstName("Test");
        testTeacher.setLastName("User");
        testTeacher.setRole(UserRole.ROLE_TEACHER);
        testTeacher.setPhoneNumber("1234567890");
        testTeacher.setNationalId("123456789");
        testTeacher.setSchool(testSchool);

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@test.com");
        testUserDto.setPassword("password123");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
        testUserDto.setRole(UserRole.ROLE_TEACHER);
        testUserDto.setPhoneNumber("1234567890");
        testUserDto.setNationalId("123456789");
        testUserDto.setSchoolId(1L);
    }

    @Test
    void toDtoShouldMapAllFields() {
        UserDto result = userMapper.toDto(testTeacher);

        assertThat(result.getId()).isEqualTo(testTeacher.getId());
        assertThat(result.getUsername()).isEqualTo(testTeacher.getUsername());
        assertThat(result.getEmail()).isEqualTo(testTeacher.getEmail());
        assertThat(result.getFirstName()).isEqualTo(testTeacher.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testTeacher.getLastName());
        assertThat(result.getRole()).isEqualTo(testTeacher.getRole());
        assertThat(result.getPhoneNumber()).isEqualTo(testTeacher.getPhoneNumber());
        assertThat(result.getNationalId()).isEqualTo(testTeacher.getNationalId());
        assertThat(result.getSchoolId()).isEqualTo(testTeacher.getSchool().getId());
    }

    @Test
    void toDtoShouldHandleNullSchool() {
        testTeacher.setSchool(null);
        UserDto result = userMapper.toDto(testTeacher);

        assertThat(result.getSchoolId()).isNull();
    }

    @Test
    void toEntityShouldCreateCorrectUserType() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isInstanceOf(Teacher.class);
        assertThat(result.getUsername()).isEqualTo(testUserDto.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUserDto.getEmail());
        assertThat(result.getPassword()).isEqualTo(testUserDto.getPassword());
        assertThat(result.getFirstName()).isEqualTo(testUserDto.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUserDto.getLastName());
        assertThat(result.getRole()).isEqualTo(testUserDto.getRole());
        assertThat(result.getPhoneNumber()).isEqualTo(testUserDto.getPhoneNumber());
        assertThat(result.getNationalId()).isEqualTo(testUserDto.getNationalId());
        assertThat(result.getSchool()).isEqualTo(testSchool);
    }

    @Test
    void toEntityShouldThrowExceptionForInvalidRole() {
        testUserDto.setRole(null);
        testUserDto.setSchoolId(null); // Prevent school lookup

        assertThatThrownBy(() -> userMapper.toEntity(testUserDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid role: null");
    }

    @Test
    void toEntityShouldThrowExceptionWhenSchoolNotFound() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userMapper.toEntity(testUserDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void toEntityShouldNotSetSchoolForAdmin() {
        testUserDto.setRole(UserRole.ROLE_ADMIN);
        testUserDto.setSchoolId(1L);

        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isInstanceOf(Admin.class);
        assertThat(result.getSchool()).isNull();
        verify(schoolRepository, never()).findById(any());
    }

    @Test
    void updateEntityFromDtoShouldUpdateAllFields() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        Teacher existingTeacher = new Teacher();
        existingTeacher.setId(1L);
        existingTeacher.setUsername("olduser");
        existingTeacher.setEmail("old@test.com");
        existingTeacher.setPassword("oldpassword");
        existingTeacher.setFirstName("Old");
        existingTeacher.setLastName("User");
        existingTeacher.setRole(UserRole.ROLE_TEACHER);
        existingTeacher.setPhoneNumber("0987654321");
        existingTeacher.setNationalId("987654321");

        userMapper.updateEntityFromDto(existingTeacher, testUserDto);

        assertThat(existingTeacher.getUsername()).isEqualTo(testUserDto.getUsername());
        assertThat(existingTeacher.getEmail()).isEqualTo(testUserDto.getEmail());
        assertThat(existingTeacher.getPassword()).isEqualTo(testUserDto.getPassword());
        assertThat(existingTeacher.getFirstName()).isEqualTo(testUserDto.getFirstName());
        assertThat(existingTeacher.getLastName()).isEqualTo(testUserDto.getLastName());
        assertThat(existingTeacher.getRole()).isEqualTo(testUserDto.getRole());
        assertThat(existingTeacher.getPhoneNumber()).isEqualTo(testUserDto.getPhoneNumber());
        assertThat(existingTeacher.getNationalId()).isEqualTo(testUserDto.getNationalId());
        assertThat(existingTeacher.getSchool()).isEqualTo(testSchool);
    }

    @Test
    void updateEntityFromDtoShouldNotUpdatePasswordWhenEmpty() {
        testUserDto.setPassword("");
        testUserDto.setSchoolId(null); // Prevent school lookup
        Teacher existingTeacher = new Teacher();
        existingTeacher.setPassword("oldpassword");

        userMapper.updateEntityFromDto(existingTeacher, testUserDto);

        assertThat(existingTeacher.getPassword()).isEqualTo("oldpassword");
    }

    @Test
    void updateEntityFromDtoShouldSetSchoolToNullForAdmin() {
        testUserDto.setRole(UserRole.ROLE_ADMIN);
        Teacher existingTeacher = new Teacher();
        existingTeacher.setSchool(testSchool);

        userMapper.updateEntityFromDto(existingTeacher, testUserDto);

        assertThat(existingTeacher.getSchool()).isNull();
        verify(schoolRepository, never()).findById(any());
    }
} 