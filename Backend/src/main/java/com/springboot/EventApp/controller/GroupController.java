package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.Enrollment;
import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.model.enums.UserLevel;
import com.springboot.EventApp.repository.EnrollmentRepository;
import com.springboot.EventApp.repository.GroupRepository;
import com.springboot.EventApp.repository.UserInfoRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * This is used for CRUD relating to groups, should not contain administrative functionalities
 * The GroupManagerController.java class refers to most of the manager functionalities.
 *
 * @author aaryamann dev sharma
 * @author Lucas Horn - completed unfinished work
 */
@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @Operation(summary = "Create a group", description = "Create a group and make the user the admin")
    @Transactional
    @PostMapping("/create/{userID}")
    public SimpleJSONResponse createGroup(@RequestBody EventGroup group, @PathVariable Long userID) {
        if (!userInfoRepository.existsById(userID)) {
            return new SimpleJSONResponse(0, "User: " + userID + " not found");
        }

        group = groupRepository.saveAndFlush(group);

        UserInfo user = userInfoRepository.findById(userID).get();
        Enrollment admin = new Enrollment(user, group, UserLevel.ADMIN);

        enrollmentRepository.save(admin);

        return new SimpleJSONResponse(1, "Group created successfully");
    }


    @Operation(summary = "Return all groups user is in", description = "Return all groups information that user is enrolled in")
    @GetMapping("/allGroups/{userID}")
    public List<EventGroup> getAllGroups(@PathVariable Long userID) {

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User does not exist");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByUser(userOpt.get());
        List<EventGroup> groups = new LinkedList<>();

        for (Enrollment e : enrollments) {
            groups.add(e.getGroup());
        }

        return groups;
    }


    @Operation(summary = "Get group info by ID", description = "Get the group information by the provided ID")
    @GetMapping("/{groupId}")
    public EventGroup getGroupDetails(@PathVariable Long groupId) {
        Optional<EventGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            return groupOpt.get();
        }
        throw new RuntimeException("Group could not be found by ID: " + groupId);
    }


    @Operation(summary = "Get users in group", description = "Get all user information for users in the group")
    @GetMapping("/{groupID}/allEnrollments")
    public List<UserInfo> getAllGroupMembers(@PathVariable Long groupID) {

        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("group: " + groupID + " not found");
        }

        EventGroup group = groupOpt.get();
        List<Enrollment> enrollments = enrollmentRepository.findByGroup(group);
        List<UserInfo> users = new LinkedList<>();

        for (Enrollment e : enrollments) {
            users.add(e.getUser());
        }

        return users;
    }


    @Operation(summary = "Get user level in group", description = "get user level in the group (0=member, 1=moderator, 2=admin)")
    @GetMapping("/{groupID}/userLevel/{userID}")
    public int getUserLevel(@PathVariable Long groupID, @PathVariable Long userID) {

        EnrollmentId enrollID = new EnrollmentId(userID, groupID);

        if (!enrollmentRepository.existsById(enrollID)) {
            throw new RuntimeException("group and user are not enrollment - not in group");
        }

        UserLevel userLevel = enrollmentRepository.findById(enrollID).get().getUserLevel();

        if (userLevel == UserLevel.MEMBER) {
            return 0;
        } else if (userLevel == UserLevel.MODERATOR) {
            return 1;
        }
        return 2;
    }


    @Operation(summary = "Find a groups subgroups", description = "Return all group information for the specified group's subgroups")
    @GetMapping("/{groupID}/subgroups")
    public Set<EventGroup> getSubgroups(@PathVariable Long groupID) {

        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group: " + groupID + " could not be found");
        }

        return groupOpt.get().getChildGroups();
    }


    @Operation(summary = "Get the parent group", description = "Get the parent groups information from the id of the subgroup")
    @GetMapping("/{groupID}/parentGroup")
    public EventGroup getParent(@PathVariable Long groupID) {

        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group: " + groupID + " could not be found");
        }

        return groupOpt.get().getParentGroup();
    }
}