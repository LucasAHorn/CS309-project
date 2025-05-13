package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This class represents an event and all related information
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "event_info")
public class EventInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "event_id")
    private long eventId;

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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "event_rsvps", joinColumns = @JoinColumn(name = "eventId"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserInfo> usersRSVPd;


    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JoinColumn(name = "poll_id", referencedColumnName = "poll_id")
    private Poll poll;

    // Getters and Setters
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

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public Set<UserInfo> getUsersRSVPd() {
        return usersRSVPd;
    }

    public void setUsersRSVPd(Set<UserInfo> usersRSVPd) {
        this.usersRSVPd = usersRSVPd;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
