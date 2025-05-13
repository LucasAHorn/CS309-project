package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.springboot.EventApp.repository.GroupRepository;
import com.springboot.EventApp.repository.UserInfoRepository;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * This is used to store messages removed in case they may need review (not implemented)
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "messages_removed")
public class RemovedMessages {
    @Id
    private Long messageId;

    @Column
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime msgTime;

    @Column
    private String message;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private UserInfo fromUser;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private EventGroup group;


    public RemovedMessages() {
    }

    public RemovedMessages(String message, Long userID, Long groupID, UserInfoRepository userRepo, GroupRepository groupRepo) {
        this.message = message;
        fromUser = userRepo.findById(userID).get();
        group = groupRepo.findById(groupID).get();
    }

    public RemovedMessages(String message, UserInfo user, EventGroup group) {
        this.message = message;
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

    public void setFromUser(UserInfo fromUser) {
        this.fromUser = fromUser;
    }

    public EventGroup getGroup() {
        return group;
    }

    public void setGroup(EventGroup group) {
        this.group = group;
    }
}
