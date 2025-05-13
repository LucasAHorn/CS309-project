package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.springboot.EventApp.util.UserUtil.*;

/**
 * This class will allow for moderation of the chat, banning and unbanning of users.
 * Please note that the deletion of a message in this class should only be used in very old messages.
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/chat")
public class MessageController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GroupMessagesRepository groupMessagesRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private EventInfoRepository eventInfoRepository;


    @Operation(summary = "Delete message from the database", description = "Lets a admin or moderator delete a chat from the database by id")
    @DeleteMapping("/{groupID}/{managerID}/{chatID}")
    public SimpleJSONResponse deleteMessage(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable Long chatID) {
        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User does not have permissions");
        }

        Optional<GroupMessages> messageOpt = groupMessagesRepository.findById(chatID);
        if (messageOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "chat does not exist");
        }

        GroupMessages message = messageOpt.get();

        if (Objects.equals(message.getGroupId(), groupID)) {
            groupMessagesRepository.deleteById(chatID);
            return new SimpleJSONResponse(1, "Chat deleted successfully");
        }

        return new SimpleJSONResponse(0, "specified message not found in specified group");
    }


    @Operation(summary = "Returns all messages from a group chat", description = "Return all messages from the group as a list")
    @GetMapping("/{groupID}/{userID}/all")
    public List<GroupMessages> getAllGroupMessages(@PathVariable Long groupID, @PathVariable Long userID) {
        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group: " + groupID + " not found");
        }

        if (!isUserInGroup(userID, groupID, enrollmentRepository)) {
            throw new RuntimeException("User " + userID + " not in group " + groupID);
        }

        List<GroupMessages> groupMessages = groupMessagesRepository.findByGroup(groupOpt.get());

        for (GroupMessages gm : groupMessages) {
            if (gm.getEvent() != null) {
                groupMessages.remove(gm);
            }
        }

        return groupMessages;
    }


    @Operation(summary = "Returns all messages from a event chat", description = "Return all messages from the group as a list")
    @GetMapping("/{groupID}/{eventID}/{userID}/all")
    public List<GroupMessages> getAllEventMessages(@PathVariable Long groupID, @PathVariable Long eventID, @PathVariable Long userID) {
        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group: " + groupID + " not found");
        }

        if (!isUserInGroup(userID, groupID, enrollmentRepository)) {
            throw new RuntimeException("User " + userID + " not in group " + groupID);
        }

        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Event " + eventID + " not found by id");
        }

        List<GroupMessages> groupMessages = groupMessagesRepository.findByGroup(groupOpt.get());

        for (int i = 0; i < groupMessages.size(); i++) {
            if (groupMessages.get(i).getEvent() != null && groupMessages.get(i).getEvent().getEventId() != eventID) {
                groupMessages.remove(i);
                i--;
            }
        }

        return groupMessages;
    }


    @Operation(summary = "Ban a user from chat", description = "Allows for a admin or moderator to ban a user (by username) from chat in the group with provided reason stored")
    @PutMapping("/{groupID}/{managerID}/{username}/{reason}")
    public SimpleJSONResponse banUserFromChat(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username, @PathVariable String reason) {

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        if (Objects.equals(userID, managerID)) {
            return new SimpleJSONResponse(0, "cannot ban yourself in chat");
        }
        if (!isUserInGroup(userID, groupID, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "User is not in group");
        }
        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager: " + managerID + " has insuffecent permissions");
        }
        if (isUserAdmin(userID, groupID, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "cannot ban admin from chat");
        }

        BannedUsers user = new BannedUsers(new EnrollmentId(userID, groupID), reason);
        bannedUsersRepository.save(user);

        return new SimpleJSONResponse(1, "user banned from chat");
    }


    @Operation(summary = "Return the ban reason", description = "Returns the ban reason in the 'message' part of a simpleJSONResponse, requires admin or moderator. Username refers to the username of the banned individual")
    @GetMapping("banReason/{groupID}/{managerID}/{username}")
    public SimpleJSONResponse getBanReason(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager does not have permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        Optional<BannedUsers> bannedUserOpt = bannedUsersRepository.findById(new EnrollmentId(userID, groupID));
        if (bannedUserOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "ban does not exist");
        }

        return new SimpleJSONResponse(1, "Reason: " + bannedUserOpt.get().getReasoning());
    }


    @Operation(summary = "Unban a user", description = "Allows a admin or moderator to unban a user from the group chat by their username")
    @DeleteMapping("unban/{groupID}/{managerID}/{username}")
    public SimpleJSONResponse unbanMember(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager does not have permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        EnrollmentId enrollmentId = new EnrollmentId(userID, groupID);

        if (!bannedUsersRepository.existsById(enrollmentId)) {
            return new SimpleJSONResponse(0, "ban does not exist");
        }

        bannedUsersRepository.deleteById(enrollmentId);

        return new SimpleJSONResponse(1, "User: " + userID + " is unbanned from group " + groupID + " chat");
    }
}