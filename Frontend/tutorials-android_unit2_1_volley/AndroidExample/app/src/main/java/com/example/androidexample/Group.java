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
public class Group {
    /** the event's ID number */
    private long groupID;
    /** the event's title */
    private String title;
    /** the event's description */
    private String description;


    //constructor without groupID(for creating new group before the backend assigns an ID)
    public Group(String title, String description){
        this.title = title;
        this.description = description;
    }

    //constructor with eventID
    public Group(String title, String description, long groupID){
        this.title = title;
        this.description = description;
        this.groupID = groupID;
    }

    public long getGroupID(){
        return groupID;
    }
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {this.description = description;}
}



