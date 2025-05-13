package com.springboot.EventApp.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

/**
 * This contains the poll information
 *
 * @author Lucas Horn
 */
@Entity
@Table(name = "poll_info")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    private Long pollId;

    @ManyToOne
    @JsonIgnore
    private EventGroup group;

    @Column
    private String prompt;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> pollOptions;

    @Column
    private boolean multiVote;

    public Poll() {
    }

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long pollId) {
        this.pollId = pollId;
    }

    public EventGroup getGroup() {
        return group;
    }

    public void setGroup(EventGroup group) {
        this.group = group;
    }

    public boolean isMultiChoice() {
        return multiVote;
    }

    public void setMultiVote(boolean multiVote) {
        this.multiVote = multiVote;
    }

    public Set<String> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(Set<String> pollOptions) {
        this.pollOptions = pollOptions;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}