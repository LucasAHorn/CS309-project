package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.springboot.EventApp.util.UserUtil.userIsAdminOrModerator;

/**
 * This class will allow users to propose events.
 * Then a manager can approve the proposal, allowing it to become an event (but not deleting the proposal, so it may be used again).
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/event/request")
public class EventRequestController {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    EventRequestRepository eventRequestRepository;

    @Autowired
    EventInfoRepository eventInfoRepository;

    /**
     * This determines if the user has perms to edit the event
     *
     * @param user         - should be admin or moderator in group, or the user that created the proposal
     * @param eventRequest - event proposal
     * @return true - they can edit, false - they cannot
     */
    private boolean userAbleToAlterProposal(UserInfo user, EventRequest eventRequest) {
        return user.getUsername().equals(eventRequest.getCreatorName()) || userIsAdminOrModerator(user.getUserId(), eventRequest.getEventGroupId(), userInfoRepository, enrollmentRepository, groupRepository);
    }


    @Operation(summary = "propose an event as member of a group", description = "All in a group can propose an event, which can then be altered by user who created it and moderators and admin, then accepted as an event by a admin or moderator")
    @PostMapping("/{userID}")
    public SimpleJSONResponse ProposeEvent(@PathVariable Long userID, @RequestBody EventRequest eventRequest) {

        if (!groupRepository.existsById(eventRequest.getEventGroupId())) {
            return new SimpleJSONResponse(0, "Group: " + eventRequest.getEventGroupId() + " does not exist");
        }

        if (!enrollmentRepository.existsById(new EnrollmentId(userID, eventRequest.getEventGroupId()))) {
            return new SimpleJSONResponse(0, "user " + userID + "not in group" + eventRequest.getEventGroupId());
        }

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user with id: " + userID + " DNE");
        }

        eventRequest.setCreatorName(userOpt.get().getUsername());

        if (eventRequest.getTitle().isEmpty() || eventRequest.getEventTime().isEmpty()) {
            return new SimpleJSONResponse(0, "Provide a title and a date and time please");
        }

        eventRequestRepository.save(eventRequest);

        return new SimpleJSONResponse(1, "request posted, you are able to edit, and delete it unless a manager approves or deletes it first.");
    }


    @Operation(summary = "Make a event from a proposal", description = "This allows for a manager or admin to make it into a real event that can be seen by all of the group")
    @PostMapping("/{proposalID}/{userID}")
    public SimpleJSONResponse makeProposalAnEvent(@PathVariable Long proposalID, @PathVariable Long userID) {

        Optional<EventRequest> eventOpt = eventRequestRepository.findById(proposalID);
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Proposal not found by Id: " + proposalID);
        }
        EventRequest eventRequest = eventOpt.get();

        if (!userIsAdminOrModerator(userID, eventRequest.getEventGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User needs to be admin or moderator to make the proposal a event");
        }

        if (eventRequest.getTitle().isEmpty() || eventRequest.getEventTime() == null || eventRequest.getDurationInMinutes() <= 0 || eventRequest.getLocation().isEmpty()) {
            return new SimpleJSONResponse(0, "Provide: title, time, day, duration, and location for an event to be created");
        }

        Optional<EventGroup> groupOpt = groupRepository.findById(eventRequest.getEventGroupId());
        if (groupOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Group not found");
        }

        EventInfo event = new EventInfo();
        event.setEventGroup(groupOpt.get());
        event.setEventTime(LocalDateTime.parse((eventRequest.getEventTime()), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        event.setCapacity((eventRequest.getCapacity() == 0) ? -1 : Math.max(-1, eventRequest.getCapacity()));
        event.setTitle(eventRequest.getTitle());
        event.setLocation(eventRequest.getLocation());
        event.setDescription(eventRequest.getDescription());
        event.setDurationInMinutes(eventRequest.getDurationInMinutes());

        eventInfoRepository.save(event);

        return new SimpleJSONResponse(1, "Event Successfully created");
    }


    @Operation(summary = "Get the events proposed by a user", description = "Get events proposed (before they have been accepted or deleted) by user (not admin or moderator view")
    @GetMapping("/{userID}")
    public List<EventRequest> getProposedEvents(@PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found by ID: " + userID);
        }

        return eventRequestRepository.findByCreatorName(userOpt.get().getUsername());
    }


    @Operation(summary = "Get group event requests (managers only)", description = "This allows for admin and moderators to get all proposed events for the specified group and see all information")
    @GetMapping("/{groupID}/{managerID}")
    public List<EventRequest> getGroupEventRequests(@PathVariable Long groupID, @PathVariable Long managerID) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            throw new RuntimeException("User needs to be a moderator or admin to use this method");
        }

        return eventRequestRepository.findAllByEventGroupId(groupID);
    }


    @Operation(summary = "Edit a event as a user or manager", description = "This allows for the creator of proposal or admin or moderators to edit the proposal before sending it to become a real event or delete it")
    @PutMapping("/{proposalID}/{userID}")
    public SimpleJSONResponse updateEvent(@PathVariable Long proposalID, @PathVariable Long userID, @RequestBody EventRequest updatedEventRequest) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User not found by ID: " + userID);
        }

        Optional<EventRequest> eventOpt = eventRequestRepository.findById(proposalID);
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Proposal not found by id: " + proposalID);
        }

        EventRequest eventRequest = eventOpt.get();

        if (!userOpt.get().getUsername().equals(eventRequest.getCreatorName()) && !userIsAdminOrModerator(userID, eventRequest.getEventGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User needs to have created the proposal or be admin or moderator in the group");
        }

        if (!Objects.equals(eventRequest.getEventGroupId(), updatedEventRequest.getEventGroupId())) {
            return new SimpleJSONResponse(0, "Request must keep the same groupId");
        }

        if (updatedEventRequest.getTitle().isEmpty() || updatedEventRequest.getEventTime().isEmpty()) {
            return new SimpleJSONResponse(0, "Provide a title and a date and time please");
        }

        eventRequestRepository.save(updatedEventRequest);

        return new SimpleJSONResponse(1, "request posted, you are able to edit, and delete it unless a manager approves or deletes it first.");
    }


    @Operation(summary = "Delete proposal as user or manager", description = "This allows for the creator or a moderator or admin in a group to delete the specified proposal for an event")
    @DeleteMapping("/{proposalID}/{userID}")
    public SimpleJSONResponse deleteUserCreatedEvent(@PathVariable Long proposalID, @PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found by ID: " + userID);
        }

        Optional<EventRequest> eventOpt = eventRequestRepository.findById(proposalID);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Proposal not found by id: " + proposalID);
        }

        EventRequest eventRequest = eventOpt.get();

        if (userOpt.get().getUsername().equals(eventRequest.getCreatorName())) {
            eventRequestRepository.deleteById(eventRequest.getRequestId());
            return new SimpleJSONResponse(1, "Event request deleted by creator of event successfully");

        } else if (userIsAdminOrModerator(userID, eventRequest.getEventGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            eventRequestRepository.deleteById(eventRequest.getRequestId());
            return new SimpleJSONResponse(1, "Event request deleted by manager of group successfully");

        } else {
            return new SimpleJSONResponse(0, "User needs to be creator of event or manager of group the event refers to");
        }
    }
}