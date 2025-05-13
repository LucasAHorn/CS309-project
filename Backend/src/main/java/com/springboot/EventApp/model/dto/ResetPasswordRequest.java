package com.springboot.EventApp.model.dto;

/**
 * This is used to update the users password
 *
 * @author aaryamann dev sharma
 * @author Lucas Horn
 */
public class ResetPasswordRequest {
    private String username;
    private String birthCity;
    private String firstPetName;
    private String newPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getFirstPetName() {
        return firstPetName;
    }

    public void setFirstPetName(String firstPetName) {
        this.firstPetName = firstPetName;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}