package com.ednevnik.dnevnik.init;

import com.ednevnik.dnevnik.model.User;
import com.ednevnik.dnevnik.model.UserRole;
import com.ednevnik.dnevnik.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@ednevnik.com");
            adminUser.setPassword(passwordEncoder.encode("shefcheto"));
            adminUser.setRole(UserRole.ROLE_ADMIN);

            userRepository.save(adminUser);
        }
    }
}
