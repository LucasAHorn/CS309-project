package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * This stores the group information, and contains the parent group (if applicable)
 *
 * @author aaryamann dev sharma
 * @author Lucas Horn
 */
@Entity
@Table(name = "event_group")
public class EventGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;


    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> groupEnrollments;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_group_id")
    private EventGroup parentGroup;

    @JsonIgnore
    @OneToMany(mappedBy = "parentGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventGroup> childGroups;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JoinColumn(name = "poll_id", referencedColumnName = "poll_id")
    private Poll poll;


    // Getters and Setters
    public Set<Enrollment> getGroupEnrollments() {
        return groupEnrollments;
    }

    public void setGroupEnrollments(Set<Enrollment> groupEnrollments) {
        this.groupEnrollments = groupEnrollments;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Set<EventGroup> getChildGroups() {
        return childGroups;
    }

    public void setChildGroups(Set<EventGroup> childGroups) {
        this.childGroups = childGroups;
    }

    public EventGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(EventGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EventGroup that = (EventGroup) obj;
        return groupId != null && groupId.equals(that.groupId);
    }
}