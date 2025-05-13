package com.springboot.EventApp.model.entities;

import com.springboot.EventApp.model.dto.EnrollmentId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

/**
 * This is used to ban users from using chats, containing the reason why and an identical key as the enrollment repo uses
 * This can find the user and group from the enrollment repository
 *
 * @author Lucas Honr
 */
@Entity
public class BannedUsers {

    @EmbeddedId
    private EnrollmentId enrollment;

    @Column
    private String reasoning;

    public BannedUsers() {
    }

    public BannedUsers(EnrollmentId enrollmentId, String reasoning) {
        this.enrollment = enrollmentId;
        this.reasoning = reasoning;
    }


    public EnrollmentId getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(EnrollmentId enrollment) {
        this.enrollment = enrollment;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
