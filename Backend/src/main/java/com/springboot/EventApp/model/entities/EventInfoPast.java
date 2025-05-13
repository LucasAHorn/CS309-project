package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This holds all past events and is updated every hour - updated from EventInfo
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "event_info_past")
public class EventInfoPast {

    @Id
    @Column(nullable = false)
    private long pastEventId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int durationInMinutes;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private EventGroup eventGroup;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "Past_Event_RSVPs", joinColumns = @JoinColumn(name = "past_event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserInfo> usersRSVPd;


    //    Constructors
    public EventInfoPast() {
    }

    public EventInfoPast(Long id, String title, String description, LocalDateTime eventDate, String location, int capacity, int durationInMinutes, EventGroup eventGroup) {
        this.pastEventId = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.durationInMinutes = durationInMinutes;
        this.eventGroup = eventGroup;
    }

    // Getters and Setters
    public long getId() {
        return pastEventId;
    }

    public void setId(long id) {
        this.pastEventId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventTime() {
        return eventDate;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventDate = eventTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public EventGroup getEventGroup() {
        return eventGroup;
    }

    public void setEventGroup(EventGroup eventGroup) {
        this.eventGroup = eventGroup;
    }

    public Set<UserInfo> getUsersRSVPd() {
        return usersRSVPd;
    }

    public void setUsersRSVPd(Set<UserInfo> usersRSVPd) {
        this.usersRSVPd = usersRSVPd;
    }
}
