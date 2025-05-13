package com.springboot.EventApp.repository;


import com.springboot.EventApp.model.entities.EventRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This connects to the event_request table
 *
 * @author Lucas Horn
 */
@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {
    List<EventRequest> findByCreatorName(String creatorName);
    List<EventRequest> findAllByEventGroupId(Long eventGroupId);
}
