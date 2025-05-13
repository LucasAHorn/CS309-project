package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.springboot.EventApp.repository.GroupRepository;
import com.springboot.EventApp.repository.UserInfoRepository;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * This contains all information regarding the group messages sent in the past
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "group_messages")
public class GroupMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long messageId;

    @Column
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime msgTime;

    @Column
    private String message;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private UserInfo fromUser;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private EventGroup group;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = true)
    private EventInfo event;

    public GroupMessages() {
    }

    public GroupMessages(String message, Long userID, Long groupID, UserInfoRepository userRepo, GroupRepository groupRepo) {
        this.message = message;
        this.msgTime = LocalDateTime.now();
        fromUser = userRepo.findById(userID).get();
        group = groupRepo.findById(groupID).get();
    }

    public GroupMessages(String message, UserInfo user, EventGroup group) {
        this.message = message;
        this.msgTime = LocalDateTime.now();
        this.fromUser = user;
        this.group = group;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(LocalDateTime msgTime) {
        this.msgTime = msgTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getFromUser() {
        return fromUser;
    }

    @JsonProperty("userId")
    public Long getUserId() {
        return fromUser.getUserId();
    }

    @JsonProperty("username")
    public String getUsername() {
        return fromUser.getUsername();
    }

    public void setFromUser(UserInfo fromUser) {
        this.fromUser = fromUser;
    }

    public EventGroup getGroup() {
        return group;
    }

    @JsonProperty("groupId")
    public Long getGroupId() {
        return group.getGroupId();
    }

    public void setGroup(EventGroup group) {
        this.group = group;
    }

    public EventInfo getEvent() {
        return event;
    }

    public void setEvent(EventInfo event) {
        this.event = event;
    }
}