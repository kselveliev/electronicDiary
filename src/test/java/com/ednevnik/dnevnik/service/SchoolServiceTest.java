package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.School;
import com.ednevnik.dnevnik.repository.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolServiceImpl schoolService;

    private School testSchool;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Test School");
        testSchool.setAddress("Test Address");
        testSchool.setPhoneNumber("1234567890");
    }

    @Test
    void saveShouldSaveSchool() {
        when(schoolRepository.save(any(School.class))).thenReturn(testSchool);

        School result = schoolService.save(testSchool);

        assertThat(result).isEqualTo(testSchool);
        verify(schoolRepository).save(testSchool);
    }

    @Test
    void findByIdShouldReturnSchoolWhenExists() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        School result = schoolService.findById(1L);

        assertThat(result).isEqualTo(testSchool);
        verify(schoolRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowExceptionWhenNotFound() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.findById(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("School not found");
    }

    @Test
    void findAllShouldReturnAllSchools() {
        List<School> schools = Arrays.asList(testSchool);
        when(schoolRepository.findAll()).thenReturn(schools);

        List<School> result = schoolService.findAll();

        assertThat(result).isEqualTo(schools);
        verify(schoolRepository).findAll();
    }

    @Test
    void deleteByIdShouldDeleteSchoolWhenExists() {
        schoolService.deleteById(1L);

        verify(schoolRepository).deleteById(testSchool.getId());
    }
} 