package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.Poll;
import com.springboot.EventApp.model.entities.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This connects to the poll_vote table
 *
 * @author Lucas Horn
 */
@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, EnrollmentId> {
    List<PollVote> findByPoll(Poll poll);
}
