package com.springboot.EventApp.model.dto;

import com.springboot.EventApp.model.entities.BannedUsers;
import com.springboot.EventApp.model.entities.GroupMessages;
import com.springboot.EventApp.model.entities.UserInfo;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * This stores the information about a user to return to frontend
 *
 * @author Lucas Horn
 */
public class GroupMemberInfo {

    private BannedUsers ban;

    private LocalDateTime joinDate;

    private UserInfo user;

    private List<GroupMessages> sentMessages = new LinkedList<>();

    public GroupMemberInfo() {
    }

    //    getters and setters
    public BannedUsers getBan() {
        return ban;
    }

    public void setBan(BannedUsers ban) {
        this.ban = ban;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public List<GroupMessages> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<GroupMessages> sentMessages) {
        this.sentMessages = sentMessages;
    }
}
