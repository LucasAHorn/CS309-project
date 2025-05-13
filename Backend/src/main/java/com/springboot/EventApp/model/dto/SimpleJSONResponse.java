package com.springboot.EventApp.model.dto;

/**
 * This class is used to return a simple response ex:
 * <p>
 * {
 * "success": 1,
 * "message": "User with ID 10 created successfully"
 * }
 * </p>
 * <p>
 * This will be very useful in many situations, additionally it has all getters and setters.
 * </p>
 *
 * @author Lucas Horn
 */
public class SimpleJSONResponse {
    private int success;
    private Long userID;
    private String message;

    // Constructor
    public SimpleJSONResponse(int success, String message, Long userID) {
        this.success = success;
        this.message = message;
        this.userID = userID;
    }

    public SimpleJSONResponse(int success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userID;
    }

    public void setUserId(Long userId) {
        this.userID = userId;
    }
}