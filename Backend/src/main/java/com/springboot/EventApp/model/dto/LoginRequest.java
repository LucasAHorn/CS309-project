package com.springboot.EventApp.model.dto;

/**
 * This is used to take login requests for users
 *
 * @author aaryamann dev sharma
 * @author Lucas Horn
 */
public class LoginRequest {
    private String username;
    private String password;

    public String getName() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}