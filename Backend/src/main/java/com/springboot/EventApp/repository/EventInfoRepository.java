package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.EventInfo;
import com.springboot.EventApp.model.entities.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for accessing and managing EventInfo entities in the database.
 *
 * @author Lucas Horn
 */
@Repository
public interface EventInfoRepository extends JpaRepository<EventInfo, Long> {
    List<EventInfo> findAllByEventGroup(EventGroup groupId);
    List<EventInfo> findByPoll(Poll poll);
    List<EventInfo> findByEventDateBefore(LocalDateTime now);
}