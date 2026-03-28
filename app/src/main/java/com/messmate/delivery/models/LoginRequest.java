package com.messmate.delivery.models;

public class LoginRequest {

    private String identifier;
    private String password;

    // Constructor
    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // Getter
    public String getIdentifier() {
        return identifier;
    }

    public String getPassword() {
        return password;
    }
}