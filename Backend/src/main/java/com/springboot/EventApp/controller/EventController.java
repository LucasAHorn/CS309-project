package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.springboot.EventApp.util.UserUtil.userIsAdminOrModerator;

/**
 * This class will support CRUD for events
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventInfoRepository eventInfoRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EventInfoPastRepository eventInfoPastRepository;

    //    HELPER METHODS
    private SimpleJSONResponse checkEventValidity(EventRequest event) {

        if (event.getTitle().isEmpty()) {
            return new SimpleJSONResponse(0, "No title provided.");
        }

        if (event.getDescription().isEmpty()) {
            return new SimpleJSONResponse(0, "No description provided.");
        }

        if (event.getEventTime() == null) {
            return new SimpleJSONResponse(0, "No time provided.");
        }

        if (event.getLocation().isEmpty()) {
            return new SimpleJSONResponse(0, "No location provided.");
        }

        if (event.getCapacity() <= 0) {
            return new SimpleJSONResponse(0, "Capacity cannot be less than 1.");
        }

        if (event.getDurationInMinutes() <= 0) {
            return new SimpleJSONResponse(0, "Duration cannot be 0 or less.");
        }

        if (event.getEventGroupId() == null) {
            return new SimpleJSONResponse(0, "Include a group");
        }

        return new SimpleJSONResponse(1, "Event functional");
    }


    private SimpleJSONResponse transferInfo(EventRequest eventRequest, EventInfo event) {
        boolean changeMade = false;

        if (eventRequest.getEventId() <= 0) {
            return new SimpleJSONResponse(0, "Event Id not provided");
        } else {
            event.setEventId(eventRequest.getEventId());
        }

        if (eventRequest.getTitle() != null && !eventRequest.getTitle().isEmpty()) {
            changeMade = true;
            event.setTitle(eventRequest.getTitle());
        }

        if (eventRequest.getDescription() != null && !eventRequest.getDescription().isEmpty()) {
            changeMade = true;
            event.setDescription(eventRequest.getDescription());
        }

        if (eventRequest.getEventTime() != null && !eventRequest.getEventTime().isEmpty()) {
            changeMade = true;

            event.setEventTime(LocalDateTime.parse(eventRequest.getEventTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (eventRequest.getLocation() != null) {
            changeMade = true;
            event.setLocation(eventRequest.getLocation());
        }

        if (eventRequest.getCapacity() > 0) {
            changeMade = true;
            event.setCapacity(eventRequest.getCapacity());
        }

        if (eventRequest.getDurationInMinutes() > 0) {
            changeMade = true;
            event.setDurationInMinutes(eventRequest.getDurationInMinutes());
        }

        if (eventRequest.getEventGroupId() != null && groupRepository.existsById(eventRequest.getEventGroupId())) {
            changeMade = true;
            event.setEventGroup(groupRepository.findById(eventRequest.getEventGroupId()).get());
        }

        return new SimpleJSONResponse(1, "Event altered");
    }


    @Operation(summary = "Create an event", description = "Creates a event (from a manager or admin)")
    @PostMapping("/create/{managerID}")
    public SimpleJSONResponse createEvent(@RequestBody EventRequest eventRQ, @PathVariable Long managerID) {

        if (eventRQ.getEventGroupId() <= 0) {
            return new SimpleJSONResponse(0, "this needs a group associated");
        }

        if (!userIsAdminOrModerator(managerID, eventRQ.getEventGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User does not have sufficient permissions");
        }

        EventInfo event = new EventInfo();

        if (eventRQ.getTitle() == null) {
            return new SimpleJSONResponse(0, "Provide a title");
        }
        event.setTitle(eventRQ.getTitle());

//        private String description (optional)
        event.setDescription(eventRQ.getDescription());

        if (eventRQ.getEventTime() == null) {
            return new SimpleJSONResponse(0, "Provide a time please");
        }
        try {
            event.setEventTime(LocalDateTime.parse(eventRQ.getEventTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            return new SimpleJSONResponse(0, "Issue parsing the date");
        }

        if (eventRQ.getLocation() == null) {
            return new SimpleJSONResponse(0, "Provide a location");
        }
        event.setLocation(eventRQ.getLocation());

        if (eventRQ.getCapacity() > 0) {
            event.setCapacity(eventRQ.getCapacity());
        } else {
            event.setCapacity(-1);
        }

        if (eventRQ.getDurationInMinutes() <= 0) {
            return new SimpleJSONResponse(0, "please provide a time");
        }
        event.setDurationInMinutes(eventRQ.getDurationInMinutes());

        if (eventRQ.getEventGroupId() != 0 && groupRepository.existsById(eventRQ.getEventGroupId())) {
            event.setEventGroup(groupRepository.findById(eventRQ.getEventGroupId()).get());
        } else {
            return new SimpleJSONResponse(0, "Provide a valid group");
        }

        try {
            eventInfoRepository.save(event);
        } catch (Exception e) {
            return new SimpleJSONResponse(0, "Error saving event");
        }

        return new SimpleJSONResponse(1, "Event created successfully.");
    }

    @Operation(summary = "Delete an event", description = "Delete an event by ID also requires the manager ID, unrecoverable")
    @DeleteMapping("/{eventID}/{managerID}")
    public SimpleJSONResponse deleteEvent(@PathVariable Long eventID, @PathVariable Long managerID) {

        if (!eventInfoRepository.existsById(eventID)) {
            return new SimpleJSONResponse(0, "ID does not match any events.");
        }

        EventInfo event = eventInfoRepository.findById(eventID).get();
        if (!userIsAdminOrModerator(managerID, event.getEventGroup().getGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User has insufficient permissions");
        }

        eventInfoRepository.deleteById(eventID);

        return new SimpleJSONResponse(1, "Event successfully deleted.");
    }


    @Operation(summary = "Remove a user RSVP from a event", description = "User removes their RSVP from an upcoming event (impossible for past events)")
    @Transactional
    @DeleteMapping("/removeRSVP/{eventID}/{userID}")
    public SimpleJSONResponse removeRSVP(@PathVariable Long eventID, @PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);

        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User not found");
        }
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Event not found");
        }

        UserInfo user = userOpt.get();
        EventInfo event = eventOpt.get();

        Set<EventInfo> eventsRSVPd = user.getEventIDsRSVPd();
        Set<UserInfo> usersRSVPd = event.getUsersRSVPd();

        if (!eventsRSVPd.contains(eventInfoRepository.findById(eventID).get())) {
            return new SimpleJSONResponse(0, "event not RSVP'd to");
        }

        eventsRSVPd.remove(event);
        usersRSVPd.remove(user);

        userInfoRepository.save(user);
        eventInfoRepository.save(event);

        return new SimpleJSONResponse(1, "Event unRSVPd");
    }


    @Operation(summary = "Edit an event as admin or moderator", description = "Admin or moderator can edit an event (by ID), requiring the event id and one change minimum")
    @PutMapping("/edit/{managerID}")
    public SimpleJSONResponse editEvent(@RequestBody EventRequest eventChanges, @PathVariable Long managerID) {

        if (eventChanges.getEventId() <= 0) {
            return new SimpleJSONResponse(0, "Event ID missing");
        }

        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventChanges.getEventId());
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Event not found");
        }
        EventInfo event = eventOpt.get();
        SimpleJSONResponse response = transferInfo(eventChanges, event);

        if (response.getSuccess() == 1) {


            if (!userIsAdminOrModerator(managerID, event.getEventGroup().getGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
                return new SimpleJSONResponse(0, "User has insufficient permissions " + "event: " + eventChanges.getEventId() + "user: " + managerID);
            }

            eventInfoRepository.save(event);
            return new SimpleJSONResponse(1, "Event updated");
        }

        return new SimpleJSONResponse(0, "No Information provided");
    }

    @Operation(summary = "RSVP to a event", description = "RSVP to event, user needs to be in the group for success")
    @Transactional
    @PutMapping("/RSVP/{eventID}/{userID}")
    public SimpleJSONResponse RSVPtoEvent(@PathVariable Long eventID, @PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);

        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User not found");
        }
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Event not found");
        }

        UserInfo user = userOpt.get();
        EventInfo event = eventOpt.get();

        user.getEventIDsRSVPd().add(event);
        event.getUsersRSVPd().add(user);

        System.out.println(user.getEventIDsRSVPd().size());
        System.out.println(event.getUsersRSVPd().size());

        userInfoRepository.save(user);
        eventInfoRepository.save(event);

        return new SimpleJSONResponse(1, "Event RSVPd");
    }

    @Operation(summary = "Get all event info by ID", description = "Get all event info, omitting RSVPs, from given EventId")
    @GetMapping("/{eventID}")
    public EventInfo getEvent(@PathVariable Long eventID) {

        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);
        if (eventOpt.isPresent()) {
            return eventOpt.get();
        }

        return null;
    }

    @Operation(summary = "Get Event Info for future events", description = "This returns events from the users groups that are in the future, RSVP'd and not")
    @GetMapping("/getFutureEvents/{userID}")
    public List<EventInfo> getFutureEvents(@PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        List<Enrollment> groups = enrollmentRepository.findByUser(userOpt.get());
        List<EventInfo> events = new LinkedList<>();
        for (Enrollment group : groups) {
            events.addAll(eventInfoRepository.findAllByEventGroup(group.getGroup()));
        }

        return events;
    }


    @Operation(summary = "Return future events RSVP'd", description = "Find all of the events RSVP'd that will take place in the future for provided user")
    @GetMapping("/RSVPdEvents/{userID}")
    public List<EventInfo> getPlannedEvents(@PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            return null;
        }

        List<EventInfo> events = new LinkedList<>();
        Optional<EventInfo> eventOpt;
        for (EventInfo event : userOpt.get().getEventIDsRSVPd()) {
            eventOpt = eventInfoRepository.findById(event.getEventId());
            if (eventOpt.isPresent()) {
                events.add(eventOpt.get());
            }
        }

        return events;
    }


    @Operation(summary = "Returns participants for a event", description = "Returns all usernames in a list that are RSVP'd to the event")
    @GetMapping("/participants/{eventID}")
    public List<String> getParticipants(@PathVariable Long eventID) {

        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Event Could not be found by id: " + eventID);
        }
        EventInfo event = eventInfoRepository.findById(eventID).get();

        List<UserInfo> users = userInfoRepository.findAll();
        List<String> usernames = new ArrayList<>();

        for (UserInfo user : users) {
            if (user.getEventIDsRSVPd().contains(event)) {
                usernames.add(user.getUsername());
            }
        }

        return usernames;
    }

    @Operation(summary = "Find event 'on deck' in users groups", description = "Find next event occurrence by time in the users groups and returns all event info")
    @GetMapping("/nextEvent/{userID}")
    public EventInfo getNextEvent(@PathVariable Long userID) {
        List<EventInfo> events = getFutureEvents(userID);

        if (events.isEmpty()) {
            throw new RuntimeException("No events found for userID: " + userID);
        }

        EventInfo nextEvent = events.get(0);
        for (EventInfo e : events) {
            if (e.getEventTime().isBefore(nextEvent.getEventTime())) {
                nextEvent = e;
            }
        }

        return nextEvent;
    }

    @Operation(summary = "Return past events RSVPd", description = "Find past events that the user has been rsvp'd to (even if they left the group)")
    @GetMapping("/past/RSVPd/{userID}")
    public Set<EventInfoPast> getPastRSVPdEvents(@PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("user: " + userID + " not found");
        }

        return userOpt.get().getPastEventRSVPs();
    }

    @Operation(summary = "Return past events RSVPd in Group", description = "Find past event RSVPs that are in the specified group and for the specific user")
    @GetMapping("/past/RSVPd/{userID}/{groupID}")
    public Set<EventInfoPast> getPastRSVPdEventsByGroup(@PathVariable Long userID, @PathVariable Long groupID) {

        Set<EventInfoPast> pastEventsRSVPd = getPastRSVPdEvents(userID);
        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);

        if (groupOpt.isEmpty()) {
            throw new RuntimeException("group: " + groupID + " not found");
        }
        EventGroup group = groupOpt.get();

        if (!enrollmentRepository.existsById(new EnrollmentId(userID, groupID))) {
            throw new RuntimeException("user: " + userID + " not in group " + group);
        }

        Set<EventInfoPast> pastGroupRSVPs = new HashSet<>();
        for (EventInfoPast p : pastEventsRSVPd) {
            if (p.getEventGroup().equals(group)) {
                pastGroupRSVPs.add(p);
            }
        }

        return pastGroupRSVPs;
    }

    @Operation(summary = "Return events from a specified group", description = "This allows for anyone to get future events from a group even if they are not within the group")
    @GetMapping("/future/groupEvents/{groupID}")
    public List<EventInfo> getAllFutureGroupEvents(@PathVariable Long groupID) {

        Optional<EventGroup> group = groupRepository.findById(groupID);
        if (group.isEmpty()) {
            throw new RuntimeException("Group not found by ID: " + groupID);
        }

        return eventInfoRepository.findAllByEventGroup(group.get());
    }

    @Operation(summary = "Return all previous events from a group", description = "This allows for anyone to see all the previous events in a group, only requiring the group id")
    @GetMapping("/past/groupEvents/{groupID}")
    public List<EventInfoPast> getAllPastGroupEvents(@PathVariable Long groupID) {

        Optional<EventGroup> group = groupRepository.findById(groupID);
        if (group.isEmpty()) {
            throw new RuntimeException("Group not found by ID: " + groupID);
        }

        return eventInfoPastRepository.findAllByEventGroup(group.get());
    }
}