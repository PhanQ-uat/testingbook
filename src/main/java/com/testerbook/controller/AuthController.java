package com.testerbook.controller;

import com.testerbook.dto.AuthRequest;
import com.testerbook.dto.AuthResponse;
import com.testerbook.dto.RegisterRequest;
import com.testerbook.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        System.out.println("[AUTH CONTROLLER] Login request received for user: " + authRequest.getUsername());
        try {
            AuthResponse authResponse = authService.login(authRequest);
            System.out.println("[AUTH CONTROLLER] Login successful for: " + authRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            System.out.println("[AUTH CONTROLLER] Login failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String username) {
        try {
            var user = authService.getCurrentUser(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
