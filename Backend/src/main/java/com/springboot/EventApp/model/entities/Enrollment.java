package com.springboot.EventApp.model.entities;

import com.springboot.EventApp.model.enums.UserLevel;

import com.springboot.EventApp.model.dto.EnrollmentId;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * This is an object which holds user enrollment in groups and their classification of member, moderator, or admin
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "enrollments")
public class Enrollment {

    // This is the userID and groupID combined
    @EmbeddedId
    private EnrollmentId enrollmentId;

    @ManyToOne
    @MapsId("userID") // Links `userID` in EnrollmentId
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @ManyToOne
    @MapsId("groupID") // Same as above (groupID)
    @JoinColumn(name = "group_id", nullable = false)
    private EventGroup group;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    @Enumerated(EnumType.STRING)  // Store as STRING instead of num
    @Column(nullable = false)
    private UserLevel userLevel;

    // constructors
    public Enrollment() {
    }

    public Enrollment(UserInfo user, EventGroup group) {
        this.enrollmentId = new EnrollmentId(user.getUserId(), group.getGroupId());
        this.user = user;
        this.group = group;
        this.enrollmentDate = LocalDateTime.now();
        this.userLevel = com.springboot.EventApp.model.enums.UserLevel.MEMBER;
    }

    public Enrollment(UserInfo user, EventGroup group, UserLevel userLevel) {
        this.enrollmentId = new EnrollmentId(user.getUserId(), group.getGroupId());
        this.user = user;
        this.group = group;
        this.enrollmentDate = LocalDateTime.now();
        this.userLevel = userLevel;
    }

    // getters and setters
    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EventGroup getGroup() {
        return group;
    }

    public void setGroup(EventGroup group) {
        this.group = group;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }

    public EnrollmentId getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(EnrollmentId enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
}
