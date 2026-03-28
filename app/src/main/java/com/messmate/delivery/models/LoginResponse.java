package com.messmate.delivery.models;

public class LoginResponse {
    private boolean success;
    private String token;
    private String type;
    private Agent agent;
    private String message;

    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public String getType() { return type; }
    public Agent getAgent() { return agent; }
    public String getMessage() { return message; }
}
