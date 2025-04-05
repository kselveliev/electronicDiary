package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.dto.UserDto;
import com.ednevnik.dnevnik.model.User;

public interface UserService {
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsername(String username);
    User createUser(UserDto userDto);
    User updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
} 