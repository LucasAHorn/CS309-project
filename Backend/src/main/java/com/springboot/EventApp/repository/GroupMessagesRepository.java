package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.model.entities.GroupMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This allows for users to access the group messages
 *
 * @author Lucas Horn
 */
@Repository
public interface GroupMessagesRepository extends JpaRepository<GroupMessages, Long> {
    List<GroupMessages> findByGroup(EventGroup group);
    List<GroupMessages> findByFromUser(UserInfo fromUser);
}
