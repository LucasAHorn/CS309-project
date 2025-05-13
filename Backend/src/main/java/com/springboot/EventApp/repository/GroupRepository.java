package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This can provide more functionality to the group repository
 *
 * @author aaryamann dev sharma
 * @author Lucas Horn
 */
@Repository
public interface GroupRepository extends JpaRepository<EventGroup, Long> {
    List<EventGroup> findByPoll(Poll poll);
}