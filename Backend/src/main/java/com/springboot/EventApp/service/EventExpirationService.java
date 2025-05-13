package com.springboot.EventApp.service;

import com.springboot.EventApp.model.entities.EventInfo;
import com.springboot.EventApp.model.entities.EventInfoPast;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.repository.EventInfoPastRepository;
import com.springboot.EventApp.repository.EventInfoRepository;
import com.springboot.EventApp.repository.UserInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class moves events that have occurred to the EventInfoPast database every minute
 *
 * @author Lucas Horn
 */
@Service
public class EventExpirationService {

    @Autowired
    private EventInfoRepository EventInfoRepository;

    @Autowired
    private EventInfoPastRepository EventInfoPastRepository;

    @Autowired
    private UserInfoRepository UserInfoRepository;

    public EventExpirationService(EventInfoRepository eventInfoRepository, EventInfoPastRepository eventInfoPastRepository) {
        this.EventInfoRepository = eventInfoRepository;
        this.EventInfoPastRepository = eventInfoPastRepository;
    }

    /**
     * This routinely moves past event to the past event database after they pass
     */
    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Transactional
    public void movePastEvents() {

        LocalDateTime now = LocalDateTime.now();
        List<EventInfo> pastEvents = EventInfoRepository.findByEventDateBefore(now);

        if (!pastEvents.isEmpty()) {

            List<EventInfoPast> pastEventsList = new LinkedList<>();
            Set<UserInfo> allUsersEdited = new HashSet<>();
            Set<UserInfo> usersRSVPd;
            Set<EventInfo> userEvents;
            Set<EventInfoPast> userPastEvents;

            for (EventInfo event : pastEvents) {
                EventInfoPast pastEvent = new EventInfoPast(event.getEventId(), event.getTitle(), event.getDescription(), event.getEventTime(), event.getLocation(), event.getCapacity(), event.getDurationInMinutes(), event.getEventGroup());

                pastEvent.setUsersRSVPd(event.getUsersRSVPd());

                usersRSVPd = event.getUsersRSVPd();
                allUsersEdited.addAll(usersRSVPd);

                try {
                    pastEventsList.add(pastEvent);
                    for (UserInfo user : usersRSVPd) {
                        userEvents = user.getEventIDsRSVPd();
                        userPastEvents = user.getPastEventRSVPs();
                        userEvents.remove(event);
                        userPastEvents.add(pastEvent);

                    }

                    System.out.println("Events Expired = " + pastEventsList.size());
                } catch (Exception e) {
                    System.err.println("Error in EventService: \n" + e.toString());
                }
            }

            EventInfoPastRepository.saveAll(pastEventsList);
            EventInfoRepository.deleteAllInBatch(pastEvents);
            UserInfoRepository.saveAll(allUsersEdited);
        }
    }
}
