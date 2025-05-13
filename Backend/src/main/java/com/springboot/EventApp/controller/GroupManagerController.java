package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.dto.GroupMemberInfo;
import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.model.enums.UserLevel;
import com.springboot.EventApp.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.springboot.EventApp.util.UserUtil.*;
import static com.springboot.EventApp.util.UserUtil.isUserAdmin;

/**
 * This class is home to the managing of a group, this contains manager and admin functions.
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/group")
public class GroupManagerController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private GroupMessagesRepository groupMessagesRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;


    @Operation(summary = "Add a member to group", description = "Allows a admin or manager to add a new member to the group")
    @Transactional
    @PutMapping("/{groupID}/{managerID}/addMember/{username}")
    public SimpleJSONResponse addMember(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager has insufficient permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        EnrollmentId enrollId = new EnrollmentId(userID, groupID);

        if (enrollmentRepository.existsById(enrollId)) {
            return new SimpleJSONResponse(0, "User already enrolled");
        }

        if (!userInfoRepository.existsById(userID)) {
            return new SimpleJSONResponse(0, "User: " + userID + " not found");

        } else if (!groupRepository.existsById(groupID)) {
            return new SimpleJSONResponse(0, "Group: " + groupID + " not found");

        }

        EventGroup group = groupRepository.findById(groupID).get();

        Enrollment newEnrollment = new Enrollment(userOpt.get(), group);
        enrollmentRepository.save(newEnrollment);

        return new SimpleJSONResponse(1, "Enrollment successful");
    }


    @Operation(summary = "Removes a member from group", description = "Admin or moderator can remove a user from the group by username (if they are not a admin)")
    @DeleteMapping("/{groupID}/{managerID}/deleteMember/{username}")
    public SimpleJSONResponse deleteMember(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager has insufficient permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        if (isUserAdmin(userID, groupID, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "user is the admin");
        }

        EnrollmentId enrollId = new EnrollmentId(userID, groupID);

        if (enrollmentRepository.existsById(enrollId)) {
            enrollmentRepository.deleteById(enrollId);
            return new SimpleJSONResponse(1, "un-enrolled successfully");
        }

        return new SimpleJSONResponse(0, "Enrollment not found\ngroupID: " + groupID + "\nUserID: " + userID);
    }


    @Operation(summary = "Delete a group", description = "Delete group from admin only, this is unrecoverable, it will remove the poll option from the parent group if it exists. Note that child groups need to be deleted first")
    @Transactional
    @DeleteMapping("/{groupId}/{adminId}")
    public SimpleJSONResponse deleteGroup(@PathVariable Long groupId, @PathVariable Long adminId) {

        if (!isUserAdmin(adminId, groupId, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "user level is not satisfactory");
        }

        EventGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        EventGroup parent = group.getParentGroup();
        if (parent != null) {
            parent.getChildGroups().remove(group);
            Poll parentPoll = parent.getPoll();
            if (parentPoll != null) {
                parentPoll.getPollOptions().remove(group.getTitle());
                List<PollVote> votes = pollVoteRepository.findByPoll(parentPoll);
                List<PollVote> toDelete = votes.stream()
                        .filter(pv -> pv.getVotes().contains(group.getTitle()))
                        .toList();
                pollVoteRepository.deleteAll(toDelete);
                pollRepository.save(parentPoll);
            }
            groupRepository.saveAndFlush(parent);
        }

        Poll groupPoll = group.getPoll();
        if (groupPoll != null) {
            List<PollVote> votes = pollVoteRepository.findByPoll(groupPoll);
            pollVoteRepository.deleteAll(votes);

            groupPoll.setGroup(null);
            pollRepository.saveAndFlush(groupPoll);

            group.setPoll(null);
            groupRepository.saveAndFlush(group);

            pollRepository.delete(groupPoll);
        }

        enrollmentRepository.deleteAllByGroup(group);

        groupRepository.delete(group);
        groupRepository.flush();

        return new SimpleJSONResponse(1, "Group deleted successfully");
    }




    @Operation(summary = "Make a group user a member", description = "Allows for admin or moderator to make a moderator a member by moderator username")
    @PutMapping("/{groupID}/{managerID}/makeMember/{username}")
    public SimpleJSONResponse makeMember(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager has insufficient permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        if (isUserAdmin(userID, groupID, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "cannot invoke on admin");
        }

        if (!groupRepository.existsById(groupID)) {
            return new SimpleJSONResponse(0, "Group: " + groupID + " not found");
        }

        EnrollmentId enrollID = new EnrollmentId(userID, groupID);

        if (!enrollmentRepository.existsById(enrollID)) {
            return new SimpleJSONResponse(0, "Member not found in group");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollID).get();
        enrollment.setUserLevel(UserLevel.MEMBER);
        enrollmentRepository.save(enrollment);

        return new SimpleJSONResponse(1, "user level updated to member");
    }


    @Operation(summary = "Make group member a moderator", description = "Allows for moderator or admin to make a member a moderator by member username")
    @PutMapping("/{groupID}/{managerID}/makeModerator/{username}")
    public SimpleJSONResponse makeModerator(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {

        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "manager has insufficient permissions");
        }

        Long userID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        userID = userOpt.get().getUserId();

        if (isUserAdmin(userID, groupID, enrollmentRepository)) {
            return new SimpleJSONResponse(0, "cannot invoke on admin");
        }

        if (!groupRepository.existsById(groupID)) {
            return new SimpleJSONResponse(0, "Group: " + groupID + " not found");
        }

        EnrollmentId enrollID = new EnrollmentId(userID, groupID);

        if (!enrollmentRepository.existsById(enrollID)) {
            return new SimpleJSONResponse(0, "user not found in group");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollID).get();
        enrollment.setUserLevel(UserLevel.MODERATOR);
        enrollmentRepository.save(enrollment);

        return new SimpleJSONResponse(1, "user level updated to moderator");
    }


    @Operation(summary = "Switch who is admin for group", description = "Allows for group admin to give admin to another group member (by member username), admin becomes a moderator")
    @Transactional
    @PutMapping("/{groupID}/{adminID}/giveAdmin/{username}")
    public SimpleJSONResponse giveAdmin(@PathVariable Long groupID, @PathVariable Long adminID, @PathVariable String username) {


        Long toUserID;
        Optional<UserInfo> userOpt = getUserByUsername(username, userInfoRepository);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "user not found by username");
        }
        toUserID = userOpt.get().getUserId();

        if (Objects.equals(adminID, toUserID)) {
            return new SimpleJSONResponse(0, "can not invoke on itself");
        }

        if (!userInfoRepository.existsById(adminID)) {
            return new SimpleJSONResponse(0, "User: " + adminID + " not found");
        }
        if (!userInfoRepository.existsById(toUserID)) {
            return new SimpleJSONResponse(0, "User: " + toUserID + " not found");
        }

        if (!groupRepository.existsById(groupID)) {
            return new SimpleJSONResponse(0, "Group: " + groupID + " not found");
        }

        EnrollmentId toEnrollID = new EnrollmentId(toUserID, groupID);
        EnrollmentId fromEnrollID = new EnrollmentId(adminID, groupID);

        if (!enrollmentRepository.existsById(fromEnrollID)) {
            return new SimpleJSONResponse(0, "provided admin not found in group");
        }
        if (!enrollmentRepository.existsById(toEnrollID)) {
            return new SimpleJSONResponse(0, "provided future admin not found in group");
        }

        Enrollment toEnrollment = enrollmentRepository.findById(toEnrollID).get();
        Enrollment fromEnrollment = enrollmentRepository.findById(fromEnrollID).get();

        if (fromEnrollment.getUserLevel() != UserLevel.ADMIN) {
            return new SimpleJSONResponse(0, "group admin not specified in 'fromUserID'");
        }

        toEnrollment.setUserLevel(UserLevel.ADMIN);
        fromEnrollment.setUserLevel(UserLevel.MODERATOR);

        enrollmentRepository.save(toEnrollment);
        enrollmentRepository.save(fromEnrollment);

        return new SimpleJSONResponse(1, "Admin swap successful");
    }


    @Operation(summary = "Create a new subgroup", description = "Group admin or moderator can create a subgroup and they automatically become the admin of the new group, this also creates or updates the group poll so users can join the subgroup through there")
    @Transactional
    @PostMapping("/{groupID}/create/{managerID}")
    public SimpleJSONResponse createSubgroup(@PathVariable Long groupID, @PathVariable Long managerID, @RequestBody EventGroup newGroup) {

        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        Optional<UserInfo> userInfoOptional = userInfoRepository.findById(managerID);
        if (groupOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "group: " + groupID + " could not be found");
        } else if (userInfoOptional.isEmpty()) {
            return new SimpleJSONResponse(0, "user: " + managerID + " could not be found");
        }

        UserInfo user = userInfoOptional.get();
        EventGroup group = groupOpt.get();
        Optional<Enrollment> userEnrollmentOpt = enrollmentRepository.findById(new EnrollmentId(managerID, groupID));

        if (userEnrollmentOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User: " + managerID + " not in group: " + groupID);
        } else if (userEnrollmentOpt.get().getUserLevel() == UserLevel.MEMBER) {
            return new SimpleJSONResponse(0, "User: " + managerID + " is not admin nor moderator in group: " + groupID);
        }

        Enrollment adminEnrollment = new Enrollment(user, newGroup, UserLevel.ADMIN);

        newGroup.setParentGroup(group);
        Set<EventGroup> group_childGroups = group.getChildGroups();
        group_childGroups.add(newGroup);

//        Create a poll option based on the title for the user to join
        Poll poll;
        if (group.getPoll() == null) {
            poll = new Poll();
            Set<String> pollOption = new HashSet<>();
            pollOption.add(newGroup.getTitle());
            poll.setPollOptions(pollOption);
            poll.setGroup(group);
            poll.setMultiVote(true);
        } else {
            poll = group.getPoll();
            poll.getPollOptions().add(newGroup.getTitle());
        }

        pollRepository.save(poll);
        groupRepository.save(newGroup);
        groupRepository.save(group);
        enrollmentRepository.save(adminEnrollment);

        return new SimpleJSONResponse(1, "Subgroup created for group: " + groupID);
    }


    @Operation(summary = "See user history in the group only", description = "Allow a manager (admin or moderator) to see the time joined group, user info, ban (if any), and messages sent")
    @GetMapping("/{groupID}/{managerID}/history/{username}")
    public GroupMemberInfo getUserGroupInfo(@PathVariable Long groupID, @PathVariable Long managerID, @PathVariable String username) {
        GroupMemberInfo memberInfo = new GroupMemberInfo();

        Optional<UserInfo> userOpt = userInfoRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("user not found by username: " + username);
        }
        memberInfo.setUser(userOpt.get());

        if (!groupRepository.existsById(groupID)) {
            throw new RuntimeException("group DNE: " + groupID);
        }

        if (!isUserInGroup(userOpt.get().getUserId(), groupID, enrollmentRepository)) {
            throw new RuntimeException("user " + username + " not in group " + groupID);
        }
        if (!userIsAdminOrModerator(managerID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            throw new RuntimeException("manager " + managerID + " not in group " + groupID);
        }

        memberInfo.setJoinDate(enrollmentRepository.findById(new EnrollmentId(userOpt.get().getUserId(), groupID)).get().getEnrollmentDate());

        Optional<BannedUsers> ban = bannedUsersRepository.findById(new EnrollmentId(userOpt.get().getUserId(), groupID));
        if (ban.isPresent()) {
            memberInfo.setBan(ban.get());
        }

        memberInfo.setSentMessages(groupMessagesRepository.findByFromUser(userOpt.get()));

        return memberInfo;
    }
}
