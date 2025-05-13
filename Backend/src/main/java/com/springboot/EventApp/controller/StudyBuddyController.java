package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.UserAndGroupInfo;
import com.springboot.EventApp.model.entities.Enrollment;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.repository.EnrollmentRepository;
import com.springboot.EventApp.repository.UserInfoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * This class helps find the classmates with the most groups in common with the provided user
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/studyBuddy")
public class StudyBuddyController {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;


    @Operation(summary = "Find the top three people with most groups in common with user", description = "This will find the top 3 people other than the user by number of groups shared. This requires only user id")
    @GetMapping("find/{userID}")
    public List<UserAndGroupInfo> getStudyBuddies(@PathVariable Long userID) {

        HashMap<UserInfo, List<String>> userAndGroup = new HashMap<>();
        List<String> groupStrings;

        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found by id " + userID);
        }
        UserInfo user = userOpt.get();

        Set<Enrollment> userEnrollments = user.getGroupEnrollments();
        for (Enrollment userEnrollment : userEnrollments) {
            List<Enrollment> otherEnrollments = enrollmentRepository.findByGroup(userEnrollment.getGroup());
            otherEnrollments.remove(userEnrollment);

            for (Enrollment otherEnrollment : otherEnrollments) {

                if (userAndGroup.containsKey(otherEnrollment.getUser())) {
                    userAndGroup.get(otherEnrollment.getUser()).add(otherEnrollment.getGroup().getTitle());
                } else {
                    List<String> groupStrs = new ArrayList<>();
                    groupStrs.add(otherEnrollment.getGroup().getTitle());
                    userAndGroup.put(otherEnrollment.getUser(), groupStrs);
                }
            }
        }

        return getUserAndGroupInfos(userAndGroup);
    }

    /**
     * @param userAndGroup - This is a hashmap containing the user and a list of groups that they are in
     * @return The top three people by amount of groups in common
     */
    private static List<UserAndGroupInfo> getUserAndGroupInfos(HashMap<UserInfo, List<String>> userAndGroup) {
        List<UserAndGroupInfo> topThree = new ArrayList<>();

        for (Map.Entry<UserInfo, List<String>> entry : userAndGroup.entrySet()) {
            UserAndGroupInfo current = new UserAndGroupInfo(entry.getKey(), entry.getValue());

            int i = 0;
            while (i < topThree.size() && entry.getValue().size() < topThree.get(i).getSharedEventGroups().size()) {
                i++;
            }
            topThree.add(i, current);

            if (topThree.size() > 3) {
                topThree.remove(3);
            }
        }

        for (UserAndGroupInfo UnG : topThree) {
            UnG.getUser().setId(null);
        }

        return topThree;
    }
}
