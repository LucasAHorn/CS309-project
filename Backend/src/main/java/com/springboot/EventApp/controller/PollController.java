package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.springboot.EventApp.service.PollService.unlinkEventPoll;
import static com.springboot.EventApp.util.UserUtil.isUserInGroup;
import static com.springboot.EventApp.util.UserUtil.userIsAdminOrModerator;

/**
 * This class holds poll CRUD functionality, there are some unique characteristics regarding update so be weary.
 *
 * @author Lucas Horn
 */
@RestController
@RequestMapping("/poll")
public class PollController {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    PollVoteRepository pollVoteRepository;

    @Autowired
    EventInfoRepository eventInfoRepository;


    @Operation(summary = "Create a poll", description = "Create a poll, as a admin or moderator for use within a event, this will automatically adjust the poll id to match the event group")
    @Transactional
    @PostMapping("/create/{groupID}/{eventID}/{userID}")
    public SimpleJSONResponse createPoll(@RequestBody Poll pollRequest, @PathVariable Long groupID, @PathVariable Long eventID, @PathVariable Long userID) {
        if (pollRequest.getPollOptions().isEmpty()) {
            return new SimpleJSONResponse(0, "Provide at least one option for users to select in the poll");
        }

        Optional<EventInfo> eventOpt = eventInfoRepository.findById(eventID);
        if (eventOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Event not found by id: " + eventID);
        }
        EventInfo event = eventOpt.get();

        Optional<EventGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "Could not find group");
        }
        EventGroup group = groupOpt.get();
        pollRequest.setGroup(group);

        if (!userIsAdminOrModerator(userID, groupID, userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "User: " + userID + " is not a admin or moderator in group: " + pollRequest.getGroup());
        }
        pollRequest.setGroup(event.getEventGroup());

        if (event.getPoll() != null) {
            return new SimpleJSONResponse(0, "poll already exists on event");
        }
        event.setPoll(pollRequest);
        pollRequest.setPollId(null);

        pollRepository.save(pollRequest);
        eventInfoRepository.save(event);

        return new SimpleJSONResponse(1, "Poll saved successfully");
    }


    @Operation(summary = "Get poll Info excluding votes", description = "This will return all poll information, but not contain votes for the options")
    @GetMapping("info/{pollID}/{userID}")
    public Poll getPoll(@PathVariable Long pollID, @PathVariable Long userID) {

        return getPoll(userID, pollID, userInfoRepository, enrollmentRepository, groupRepository, pollRepository);
    }


    @Operation(summary = "Get votes for a poll separated by poll options", description = "This returns all of the poll votes grouped by option in a hashmap, should be easy to update numbers via a socket")
    @GetMapping("votes/{pollID}/{userID}")
    public HashMap<String, Long> getPollOptionVoteCounts(@PathVariable Long pollID, @PathVariable Long userID) {

        Poll poll = getPoll(pollID, userID);

        List<PollVote> votes = pollVoteRepository.findByPoll(poll);

        HashMap<String, Long> responseMap = new HashMap<>();

        for (PollVote pv : votes) {
            for (String vote : pv.getVotes()) {
                if (!responseMap.containsKey(vote)) {
                    responseMap.put(vote, 1L);
                } else {
                    responseMap.put(vote, responseMap.get(vote) + 1);
                }
            }
        }

        return responseMap;
    }


    @Operation(summary = "Change the options in a poll", description = "This allows for options to be changed in a poll, deleting all previous responses")
    @Transactional
    @PutMapping("/{pollID}/{userID}")
    public SimpleJSONResponse changePollAnswersAndDeleteCurrentResponses(@RequestBody Set<String> pollOptions, @PathVariable Long pollID, @PathVariable Long userID) {

        Poll poll = getPoll(pollID, userID);

        if (!userIsAdminOrModerator(userID, poll.getGroup().getGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "user: " + userID + " is not admin or moderator in group: " + poll.getGroup().getGroupId());
        }

        if (pollOptions.isEmpty()) {
            return new SimpleJSONResponse(0, "Need to provide at least one poll option");
        }

        poll.setPollOptions(pollOptions);

        pollVoteRepository.deleteAll(pollVoteRepository.findByPoll(poll));
        pollRepository.save(poll);

        return new SimpleJSONResponse(1, "Poll options changed and user votes reset");
    }


    @Operation(summary = "Delete a poll as an admin", description = "This allows for a manager or admin to delete a poll")
    @Transactional
    @DeleteMapping("/{pollID}/{userID}")
    public SimpleJSONResponse deletePollById(@PathVariable Long pollID, @PathVariable Long userID) {

        Poll poll = getPoll(pollID, userID);
        if (!userIsAdminOrModerator(userID, poll.getGroup().getGroupId(), userInfoRepository, enrollmentRepository, groupRepository)) {
            return new SimpleJSONResponse(0, "user " + userID + " is not a admin or group: " + poll.getGroup().getGroupId());
        }

        List<EventInfo> events = eventInfoRepository.findByPoll(poll);
        for (EventInfo event : events) {
            event.setPoll(null);
        }
        eventInfoRepository.saveAll(events);

        unlinkEventPoll(pollID, pollRepository, groupRepository);
        pollVoteRepository.deleteAll(pollVoteRepository.findByPoll(poll));
        pollRepository.deleteById(pollID);

        return new SimpleJSONResponse(1, "Poll successfully deleted");
    }


    public static Poll getPoll(Long userID, Long pollID, UserInfoRepository userInfoRepository, EnrollmentRepository enrollmentRepository, GroupRepository groupRepository, PollRepository pollRepository) {

        Optional<Poll> pollOpt = pollRepository.findById(pollID);

        if (pollOpt.isEmpty()) {
            throw new RuntimeException("Poll not found with id: " + pollID);
        }
        Poll poll = pollOpt.get();

        if (!isUserInGroup(userID, poll.getGroup().getGroupId(), enrollmentRepository)) {
            throw new RuntimeException("User: " + userID + " not found in group: " + poll.getGroup().getGroupId());
        }

        return poll;
    }


    public static boolean addVoteToPoll(Long userID, Long pollID, String selection, UserInfoRepository userInfoRepository, EnrollmentRepository enrollmentRepository, GroupRepository groupRepository, PollRepository pollRepository, PollVoteRepository pollVoteRepository) {

        try {

            Poll poll = getPoll(userID, pollID, userInfoRepository, enrollmentRepository, groupRepository, pollRepository);

            if (!poll.getPollOptions().contains(selection)) {
                System.out.println("Poll option provided that did not exist in poll");
                return false;
            }

            List<PollVote> voters = pollVoteRepository.findByPoll(poll);

            PollVote existingVote = null;

            for (PollVote pv : voters) {
                if (Objects.equals(pv.getUser().getUserId(), userID)) {
                    existingVote = pv;
                    break;
                }
            }

            if (existingVote != null) {
                if (!poll.isMultiChoice()) {
                    // Single-choice: user already voted — don't allow another vote
                    return false;
                }

                if (!existingVote.getVotes().contains(selection)) {
                    existingVote.getVotes().add(selection);
                    pollVoteRepository.save(existingVote);
                    return true;
                } else {
                    return false;
                }
            }

            Set<String> voteStrs = new HashSet<>();
            voteStrs.add(selection);

            PollVote newVote = new PollVote();
            newVote.setPoll(poll);

            Optional<UserInfo> user = userInfoRepository.findById(userID);
            if (user.isEmpty()) {
                throw new RuntimeException("The user was not found by id: " + userID);
            }

            newVote.setUser(user.get());
            newVote.setVotes(voteStrs);

            pollVoteRepository.save(newVote);
            return true;

        } catch (Exception e) {
            System.out.println("Error in PollController.addVoteToPoll()");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param userID - user Id
     * @param pollID - poll Id
     * @param selection - choice voted for
     * @param userInfoRepository - user_info table
     * @param enrollmentRepository - enrollment table
     * @param groupRepository - group table
     * @param pollRepository - poll_info table
     * @param pollVoteRepository - poll_vote table
     * @return if the user selection was removed from the poll
     */
    public static boolean removeVoteFromPoll(Long userID, Long pollID, String selection, UserInfoRepository userInfoRepository, EnrollmentRepository enrollmentRepository, GroupRepository groupRepository, PollRepository pollRepository, PollVoteRepository pollVoteRepository) {

        Poll poll = getPoll(userID, pollID, userInfoRepository, enrollmentRepository, groupRepository, pollRepository);

        List<PollVote> voters = pollVoteRepository.findByPoll(poll);

        for (PollVote pv : voters) {
            if (Objects.equals(pv.getUser().getUserId(), userID)) {
                if (pv.getVotes().contains(selection)) {
                    pv.getVotes().remove(selection);
                    if (pv.getVotes().isEmpty()) {
                        pollVoteRepository.delete(pv);
                    } else {
                        pollVoteRepository.save(pv);
                    }
                    return true;
                }
            }
        }
        throw new RuntimeException("Vote not found from voter " + userID + " in poll " + pollID);
    }
}
