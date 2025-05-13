package com.springboot.EventApp.model.entities;

import jakarta.persistence.*;

/**
 * This class is used only for editing events
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "event_requests")
public class EventRequest {

    // used only in the edit requests
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private Long eventId;

    // Required
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long eventGroupId;

    @Column(nullable = false)
    private String eventDate;

    // Remainder not required
    @Column
    private String description;

    @Column
    private String location;

    @Column
    private int capacity;

    @Column
    private int durationInMinutes;

    @Column
    private String creatorName;

    //    Constructor
    public EventRequest() {
    }

    //    Getters and Setters
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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

    public String getEventTime() {
        return eventDate;
    }

    public void setEventTime(String eventTime) {
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

    public Long getEventGroupId() {
        return eventGroupId;
    }

    public void setEventGroupId(Long eventGroupId) {
        this.eventGroupId = eventGroupId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}