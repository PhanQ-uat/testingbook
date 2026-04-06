package com.testerbook.dto;

import com.testerbook.model.UserRole;

public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String username, String email, UserRole role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
