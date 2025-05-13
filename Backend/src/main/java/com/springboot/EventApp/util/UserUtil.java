package com.springboot.EventApp.util;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.Enrollment;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.model.enums.UserLevel;
import com.springboot.EventApp.repository.BannedUsersRepository;
import com.springboot.EventApp.repository.EnrollmentRepository;
import com.springboot.EventApp.repository.GroupRepository;
import com.springboot.EventApp.repository.UserInfoRepository;

import java.util.Optional;


/**
 * This exists to make dealing with users and group levels easier
 *
 * @author Lucas Horn
 */
public class UserUtil {

    /**
     * This finds if a user has permissions to edit or create group events
     *
     * @param managerID - manager id
     * @param groupId   - group id
     * @return true: manager is admin or moderator; false: manager is user (not permitted to edit or create things)
     */
    public static boolean userIsAdminOrModerator(Long managerID, Long groupId, UserInfoRepository userInfoRepository, EnrollmentRepository enrollmentRepository, GroupRepository groupRepository) {

        if (userInfoRepository.existsById(managerID) && groupRepository.existsById(groupId)) {

            Optional<Enrollment> enrollOpt = enrollmentRepository.findById(new EnrollmentId(managerID, groupId));
            if (enrollOpt.isEmpty()) {
                return false;
            }
            return enrollOpt.get().getUserLevel() == UserLevel.ADMIN || enrollOpt.get().getUserLevel() == UserLevel.MODERATOR;

        }
        return false;
    }

    /**
     * @param userID               - user id
     * @param groupId              - group id
     * @param enrollmentRepository - enrollment (what groups user is in) repo
     * @return true - user in group, false - user not in group
     */
    public static boolean isUserInGroup(Long userID, Long groupId, EnrollmentRepository enrollmentRepository) {
        return enrollmentRepository.existsById(new EnrollmentId(userID, groupId));
    }

    /**
     * @param userID               - user id
     * @param groupId              - group id
     * @param enrollmentRepository - enrollment table (user and groups)
     * @return if the user is admin
     */
    public static boolean isUserAdmin(Long userID, Long groupId, EnrollmentRepository enrollmentRepository) {

        Optional<Enrollment> enrollOpt = enrollmentRepository.findById(new EnrollmentId(userID, groupId));

        if (enrollOpt.isEmpty()) {
            return false;
        }

        return enrollOpt.get().getUserLevel() == UserLevel.ADMIN;
    }

    /**
     * @param userID     - user id
     * @param groupID    - group id
     * @param bannedRepo - banned members repository
     * @return true - the user is banned, false - user is not banned
     */
    public static boolean isUserBannedFromChat(Long userID, Long groupID, BannedUsersRepository bannedRepo) {
        return bannedRepo.existsById(new EnrollmentId(userID, groupID));
    }

    /**
     * Find the user by username
     *
     * @param username           - username
     * @param userInfoRepository - JPA repo
     * @return User - if it exists
     */
    public static Optional<UserInfo> getUserByUsername(String username, UserInfoRepository userInfoRepository) {
        return userInfoRepository.findByUsername(username);
    }
}