package com.springboot.EventApp.model.dto;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * This represents the id for enrollment objects
 *
 * @author Lucas Horn
 */
@Embeddable
public class EnrollmentId implements Serializable {

    private Long userID;

    private Long groupID;

    public EnrollmentId() {
    }

    public EnrollmentId(Long userID, Long groupID) {
        this.userID = userID;
        this.groupID = groupID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnrollmentId that)) return false;
        return Objects.equals(userID, that.userID) && Objects.equals(groupID, that.groupID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, groupID);
    }
//    end of code that may not be needed

    // getters and setters
    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }
}