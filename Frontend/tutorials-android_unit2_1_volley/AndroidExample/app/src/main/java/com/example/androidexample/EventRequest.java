package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public class EventRequest {
    private long requestId;
    private String title;
    private String description;
    private String eventTime;
    private long requesterId;
    private long groupId;

    // Constructor for manual creation (optional)
    public EventRequest(long requestId, String title, String description, String eventTime,
                        long requesterId, long groupId) {
        this.requestId = requestId;
        this.title = title;
        this.description = description;
        this.eventTime = eventTime;
        this.requesterId = requesterId;
        this.groupId = groupId;
    }

    // Constructor to parse from JSONObject
    public EventRequest(JSONObject json) throws JSONException {
        this.requestId = json.getLong("requestId");
        this.title = json.getString("title");
        this.description = json.getString("description");
        this.eventTime = json.getString("eventTime");
        this.groupId = json.getLong("eventGroupId");
    }

    // Getters and setters
    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
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
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(long requesterId) {
        this.requesterId = requesterId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    // Optional: For logging or debugging
    @Override
    public String toString() {
        return "EventRequest{" +
                "requestId=" + requestId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", requesterId=" + requesterId +
                ", groupId=" + groupId +
                '}';
    }
}
