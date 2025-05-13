package com.example.androidexample;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * This Event class represents an event in the application.
 * It contains details like event's title, eventDate(including Time), location, description, capacity,
 * durationInMinutes, and groupID.
 *
 * @author Lauren Kwon
 *
 */
public class Event implements Comparable<Event> {
    /** the event's ID number */
    private long eventID;
    /** the event's title */
    private String title;
    /** the event's location */
    private String location;
    /** the event's description */
    private String description;
    /** the event's date */
    private LocalDateTime eventTime;
    /** the event's maximum participants */
    private int capacity;
    /** the event's duration in minutes */
    private int durationInMinutes;
    /** the event's group ID */
    private long groupID;

    /** the DateTimeFormatter in M/d/yyyy HH:mm format */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy HH:mm");
    /** the DateTimeFormatter in HH:mm format */
    private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern("HH:mm");
    /** the DateTimeFormatter in yyyy-MM-dd HH:mm:ss format */
    private static final DateTimeFormatter BACKEND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    /**
     * Constructor for creating a new event without an assigned eventID.
     * @param title the event's title
     * @param eventTime the event's date and time.
     * @param location the event's location
     * @param description the event's description
     * @param capacity the event's maximum participants
     * @param durationInMinutes the event's duration time in minutes.
     * @param groupID the event's groupID
     */
    //constructor without eventID(for creating new events before the backend assigns an ID)
    public Event(String title, LocalDateTime eventTime, String location, String description, int capacity, int durationInMinutes, long groupID){
        this.title = title;
        this.eventTime = eventTime;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.durationInMinutes = durationInMinutes;
        this.groupID = groupID;
    }

    /**
     * Constructor for creating an event with an assigned eventID.
     * @param eventID the event's ID that was created by the backend server.
     * @param title the event's title
     * @param eventTime the event's date and time.
     * @param location the event's location
     * @param description the event's description
     * @param capacity the event's maximum participants
     * @param durationInMinutes the event's duration time in minutes.
     * @param groupID the event's groupID
     */
    //constructor with eventID
    public Event(long eventID, String title, LocalDateTime eventTime, String location, String description, int capacity, int durationInMinutes, long groupID) {
        this.eventID = eventID;
        this.title = title;
        this.eventTime = eventTime;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.durationInMinutes = durationInMinutes;
        this.groupID = groupID;
    }
    public long getEventID(){
        return eventID;
    }
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public LocalDateTime getEventTime() {
        return eventTime;
    }
    public String getEventDateStr() {
        return eventTime.format(BACKEND_FORMATTER);
    }
    public void setEventTime(String eventDateStr) {
        try {
            // First try parsing with backend defined format
            this.eventTime = LocalDateTime.parse(eventDateStr, BACKEND_FORMATTER);
        } catch (Exception e1) {
            try {
                // Fallback: Try other format
                this.eventTime = LocalDateTime.parse(eventDateStr, FORMATTER);
            } catch (Exception e2) {
                // Final fallback: Set current date/time
                this.eventTime = LocalDateTime.now();
                e2.printStackTrace();
            }
        }
    }


    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {this.location = location;}

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {this.description = description;}

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {this.capacity = capacity;}

    public int getDurationInMinutes(){return durationInMinutes;}
    public void setDurationInMinutes(int mins) {this.durationInMinutes = mins;}
    public long getGroupID(){
        return groupID;
    }
    public String getEndTime() {
        if (this.eventTime == null) return "N/A"; // Handle null eventDate
        LocalDateTime endEventTime = this.eventTime.plusMinutes(durationInMinutes);
        return endEventTime.format(FORMATTER1);
    }

    @Override
    public int compareTo(Event other) {
        if (this.eventTime == null || other.eventTime == null) {
            return 0; // Consider them equal if either date is null
        }
        return this.eventTime.compareTo(other.eventTime);
    }

}



