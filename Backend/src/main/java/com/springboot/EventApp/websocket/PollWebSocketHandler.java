package com.springboot.EventApp.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.*;
import com.springboot.EventApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.springboot.EventApp.controller.PollController.*;
import static com.springboot.EventApp.util.UserUtil.isUserInGroup;
import static com.springboot.EventApp.util.UserUtil.userIsAdminOrModerator;

/**
 * This handles poll functionality regarding the addition and deletion of a vote in a websocket
 *
 * @author Lucas Horn
 */
@Component
public class PollWebSocketHandler extends TextWebSocketHandler {

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

    private final Map<Long, List<WebSocketSession>> pollSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();  // e.g., /ws/poll/42
            String query = session.getUri().getQuery(); // e.g., userId=99

            Long pollId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

            if (query == null) {
                session.sendMessage(new TextMessage("Missing query string."));
                session.close();
                return;
            }

            Map<String, String> queryParams = Arrays.stream(query.split("&")).map(param -> param.split("=", 2)).filter(arr -> arr.length == 2).collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

            String userIdStr = queryParams.get("userId");
            if (userIdStr == null) {
                session.sendMessage(new TextMessage("Missing userId in query."));
                session.close();
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            Poll poll;
            try {
                poll = getPoll(userId, pollId, userInfoRepository, enrollmentRepository, groupRepository, pollRepository);
            } catch (Exception e) {
                session.sendMessage(new TextMessage("user: " + userId + " not in group associated with the poll: " + pollId));
                return;
            }


            HashMap<String, Long> voteCounts = new HashMap<>();
            Set<String> votes;

            for (PollVote pv : pollVoteRepository.findByPoll(poll)) {
                votes = pv.getVotes();
                for (String vote : votes) {
                    voteCounts.put(vote, voteCounts.getOrDefault(vote, 0L) + 1);
                }
            }

            for (String pollOption : poll.getPollOptions()) {
                voteCounts.putIfAbsent(pollOption, 0L);
            }

            ObjectNode message = new ObjectMapper().createObjectNode();
            for (Map.Entry<String, Long> pair : voteCounts.entrySet()) {
                message.put(pair.getKey(), pair.getValue());
            }

            String jsonResponse = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonResponse));


            pollSessions.computeIfAbsent(pollId, k -> new ArrayList<>()).add(session);

        } catch (Exception e) {
            System.out.println("Error in poll WebSocket connection: " + e.getMessage());
            try {
                session.close();
            } catch (Exception closeEx) {
                System.out.println("Error closing session: " + closeEx.getMessage());
            }
        }
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String path = session.getUri().getPath();
        Long groupId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));


        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        Iterator<String> keyIterator = jsonNode.fieldNames();
        ArrayList<String> msgKeys = new ArrayList<>();
        while (keyIterator.hasNext()) {
            msgKeys.add(keyIterator.next());
        }

        ObjectNode response = new ObjectMapper().createObjectNode();

        if (msgKeys.contains("userId") && msgKeys.contains("pollId") && msgKeys.contains("voteString")) {

            Long userId = jsonNode.get("userId").asLong();
            Long pollId = jsonNode.get("pollId").asLong();
            String vote = jsonNode.get("voteString").asText();

            if (!addVoteToPoll(userId, pollId, vote, userInfoRepository, enrollmentRepository, groupRepository, pollRepository, pollVoteRepository)) {
                session.sendMessage(new TextMessage("Failed to add vote to poll"));
                return;
            }

            response.put("addedVote", vote);


            Optional<Poll> pollOpt = pollRepository.findById(pollId);
            if (pollOpt.isEmpty()) {
                throw new RuntimeException("Poll not found");
            }
            Poll poll = pollOpt.get();
            if (poll.getGroup().getPoll() != null && Objects.equals(poll.getGroup().getPoll().getPollId(), pollId)) {

                Set<EventGroup> subGroups = poll.getGroup().getChildGroups();
                EventGroup subGroup = null;
                for (EventGroup e : subGroups) {
                    if (Objects.equals(e.getTitle(), vote)) {
                        subGroup = e;
                    }
                }
                if (subGroup == null) {
                    throw new RuntimeException("Poll subgroup not found");
                }

                if (!isUserInGroup(userId, groupId, enrollmentRepository)) {

                    Optional<UserInfo> userOpt = userInfoRepository.findById(userId);
                    if (userOpt.isEmpty()) {
                        throw new RuntimeException("User not found");
                    }
                    UserInfo user = userOpt.get();

                    Enrollment enrollment = new Enrollment(user, subGroup);
                    enrollmentRepository.save(enrollment);

                    user.getGroupEnrollments().add(enrollment);
                    userInfoRepository.save(user);

                } else {
                    session.sendMessage(new TextMessage("user already in group"));
                }
            }

        } else if (msgKeys.contains("userId") && msgKeys.contains("pollId") && msgKeys.contains("removeVote")) {

            Long userId = jsonNode.get("userId").asLong();
            Long pollId = jsonNode.get("pollId").asLong();
            String removeVote = jsonNode.get("removeVote").asText();

            if (!removeVoteFromPoll(userId, pollId, removeVote, userInfoRepository, enrollmentRepository, groupRepository, pollRepository, pollVoteRepository)) {
                session.sendMessage(new TextMessage("Failed to remove vote from poll"));
                return;
            }

            response.put("removedVote", removeVote);

            Optional<Poll> pollOpt = pollRepository.findById(pollId);
            if (pollOpt.isEmpty()) {
                throw new RuntimeException("Poll not found");
            }
            Poll poll = pollOpt.get();
            if (poll.getGroup().getPoll() != null && Objects.equals(poll.getGroup().getPoll().getPollId(), pollId)) {

                Set<EventGroup> subGroups = poll.getGroup().getChildGroups();
                EventGroup subGroup = null;
                for (EventGroup e : subGroups) {
                    if (Objects.equals(e.getTitle(), removeVote)) {
                        subGroup = e;
                    }
                }
                if (subGroup == null) {
                    throw new RuntimeException("Poll subgroup not found");
                }

                if (isUserInGroup(userId, groupId, enrollmentRepository) && !userIsAdminOrModerator(userId, groupId, userInfoRepository, enrollmentRepository, groupRepository)) {

                    Optional<UserInfo> userOpt = userInfoRepository.findById(userId);
                    if (userOpt.isEmpty()) {
                        throw new RuntimeException("User not found");
                    }
                    UserInfo user = userOpt.get();

                    Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(new EnrollmentId(userId, groupId));
                    if (enrollmentOpt.isEmpty()) {
                        throw new RuntimeException("Enrollment not found");
                    }

                    enrollmentRepository.delete(enrollmentOpt.get());
                    user.getGroupEnrollments().remove(enrollmentOpt.get());
                    userInfoRepository.save(user);

                } else {
                    session.sendMessage(new TextMessage("user already in group"));
                }
            }
        } else {
            session.sendMessage(new TextMessage("Provide keys: userId, pollId, and voteString (the string option in the poll)"));
            return;
        }

        String jsonResponse = objectMapper.writeValueAsString(response);

        try {
            Long pollId = jsonNode.get("pollId").asLong();

            for (WebSocketSession s : pollSessions.get(pollId)) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(jsonResponse));
                }
            }
        } catch (Exception e) {
            System.out.println("error sending message to update polls");
        }
    }


    /**
     * This updates all the polls every minute in case the options have changed or a message was misse
     *
     * @throws Exception - I sure hope not
     */
    @Scheduled(cron = "0 * * * * *")
    public void updateAllInfo() throws Exception {

        ObjectNode message;
        Set<String> votes;
        HashMap<String, Long> voteCounts = new HashMap<>();

        for (Map.Entry<Long, List<WebSocketSession>> entry : pollSessions.entrySet()) {
            Long pollId = entry.getKey();
            List<WebSocketSession> sessions = entry.getValue();
            message = new ObjectMapper().createObjectNode();

            Optional<Poll> pollOpt = pollRepository.findById(pollId);
            if (pollOpt.isEmpty()) {
                System.out.println("Poll: " + pollId + " not found, closing all related sessions");
                for (WebSocketSession s : sessions) {
                    afterConnectionClosed(s, CloseStatus.SERVER_ERROR);
                }
                pollSessions.remove(pollId);
            }
            Poll poll = pollOpt.get();

            for (PollVote pv : pollVoteRepository.findByPoll(poll)) {

                votes = pv.getVotes();
                for (String vote : votes) {
                    if (voteCounts.containsKey(vote)) {
                        voteCounts.put(vote, voteCounts.get(vote) + 1);
                    } else {
                        voteCounts.put(vote, 1L);
                    }
                }
            }

            for (String pollOption : poll.getPollOptions()) {
                if (!voteCounts.containsKey(pollOption)) {
                    voteCounts.put(pollOption, 0L);
                }
            }

            for (Map.Entry<String, Long> pair : voteCounts.entrySet()) {
                message.put(pair.getKey(), pair.getValue());
            }

            String jsonResponse = objectMapper.writeValueAsString(message);

            for (WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage(jsonResponse));
            }
            voteCounts = new HashMap<>();
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        pollSessions.values().forEach(sessions -> sessions.remove(session));
    }
}
