package com.ednevnik.dnevnik.service;

import com.ednevnik.dnevnik.model.User;

public interface UserService {
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsername(String username);
} 