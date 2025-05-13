package com.springboot.EventApp.model.dto;

import com.springboot.EventApp.model.entities.UserInfo;

import java.util.List;

/**
 * This is used in StudyBuddyController to return data to the frontend
 *
 * @author Lucas Horn
 */
public class UserAndGroupInfo {

    private UserInfo user;

    private List<String> sharedEventGroups;

    public UserAndGroupInfo() {
    }

    public UserAndGroupInfo(UserInfo user, List<String> sharedEventGroups) {
        this.user = user;
        this.sharedEventGroups = sharedEventGroups;
    }


    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public List<String> getSharedEventGroups() {
        return sharedEventGroups;
    }

    public void setSharedEventGroups(List<String> sharedEventGroups) {
        this.sharedEventGroups = sharedEventGroups;
    }
}
