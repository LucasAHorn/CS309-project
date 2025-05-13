package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.RemovedMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This allows for the removed messages to be accessed
 *
 * @author Lucas Horn
 */
@Repository
public interface RemovedMessagesRepository extends JpaRepository<RemovedMessages, Long> {
}
