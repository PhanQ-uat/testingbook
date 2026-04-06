package com.testerbook.config;

import com.testerbook.model.User;
import com.testerbook.model.UserRole;
import com.testerbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@testerbook.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setBio("System Administrator");
            admin.setRole(UserRole.ADMIN);
            
            userRepository.save(admin);
            System.out.println("✓ Default admin user created: username='admin', password='admin'");
        } else {
            System.out.println("✓ Admin user already exists");
        }
    }
}
