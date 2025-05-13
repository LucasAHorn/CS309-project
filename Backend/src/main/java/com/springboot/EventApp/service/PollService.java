package com.springboot.EventApp.service;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.Poll;
import com.springboot.EventApp.repository.EventInfoRepository;
import com.springboot.EventApp.repository.GroupRepository;
import com.springboot.EventApp.repository.PollRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This helps unlink the poll from groups so that it can be deleted
 *
 * @author Lucas Horn
 */
@Service
public class PollService {

    /**
     * This helps unlink the poll from groups so that it can be deleted
     *
     * @param pollId               - poll id
     * @param pollRepository       - repository linking to poll_info
     * @param eventGroupRepository - repository linking to groups
     */
    @Transactional
    public static void unlinkEventPoll(Long pollId, PollRepository pollRepository, GroupRepository eventGroupRepository) {
        Poll poll = pollRepository.findById(pollId).orElseThrow();
        List<EventGroup> groups = eventGroupRepository.findByPoll(poll);
        for (EventGroup group : groups) {
            group.setPoll(null);
        }
        eventGroupRepository.saveAll(groups);
    }
}
