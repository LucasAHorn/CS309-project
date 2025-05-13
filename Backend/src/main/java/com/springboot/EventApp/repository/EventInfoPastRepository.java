package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.EventInfoPast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This can add functionality to the past event repository
 *
 * @author Lucas Horn
 */
@Repository
public interface EventInfoPastRepository extends JpaRepository<EventInfoPast, Long> {
    List<EventInfoPast> findAllByEventGroup(EventGroup groupId);
}
