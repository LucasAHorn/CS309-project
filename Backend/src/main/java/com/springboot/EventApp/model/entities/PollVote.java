package com.springboot.EventApp.model.entities;

import jakarta.persistence.*;

import java.util.Set;

/**
 * This is used for users to vote on polls
 *
 * @author Lucas Horn
 */
@Entity
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long pollVoteId;

    @ManyToOne
    private Poll poll;

    @ManyToOne
    private UserInfo user;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> Votes;

    public PollVote() {
    }


    public Long getPollVoteId() {
        return pollVoteId;
    }

    public void setPollVoteId(Long pollVoteId) {
        this.pollVoteId = pollVoteId;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public Set<String> getVotes() {
        return Votes;
    }

    public void setVotes(Set<String> votes) {
        Votes = votes;
    }
}
