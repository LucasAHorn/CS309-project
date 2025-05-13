package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * This is meant to represent a user object that is stored in the sql database
 * </p>
 * <p>
 * This contains:
 * Long id (auto generated, unique)
 * String email
 * String username (unique)
 * String password (sha256 hash)
 * String birthCity (sha256 hash)
 * String firstPetNAme (sha256 hash)
 * </p>
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "user_info", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String birthCity;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String firstPetName;


    // complex relationships
    @JsonIgnore // Prevents infinite recursion when serializing JSON
    @ManyToMany(mappedBy = "usersRSVPd")
    private Set<EventInfo> eventRSVPs = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "usersRSVPd")
    private Set<EventInfoPast> pastEventRSVPs = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> groupEnrollments = new HashSet<>();


    // Default constructor
    public UserInfo() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Set<EventInfo> getEventIDsRSVPd() {
        return eventRSVPs;
    }

    public void setEventIDsRSVPd(Set<EventInfo> eventRSVPs) {
        this.eventRSVPs = eventRSVPs;
    }

    public Set<Enrollment> getGroupEnrollments() {
        return groupEnrollments;
    }

    public void setGroupEnrollments(Set<Enrollment> groupEnrollments) {
        this.groupEnrollments = groupEnrollments;
    }

    public Set<EventInfoPast> getPastEventRSVPs() {
        return pastEventRSVPs;
    }

    public void setPastEventRSVPs(Set<EventInfoPast> pastEventRSVPs) {
        this.pastEventRSVPs = pastEventRSVPs;
    }

    @Override
    public UserInfo clone() {
        try {
            UserInfo cloned = (UserInfo) super.clone(); // Perform shallow copy
            cloned.setEmail(this.email);
            cloned.setUsername(this.username);
            cloned.setPassword(this.password);
            cloned.setBirthCity(this.birthCity);
            cloned.setFirstPetName(this.firstPetName);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported for UserInfo", e);
        }
    }
}