package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This connects to the poll_info table
 *
 * @author Lucas Horn
 */
@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
}
