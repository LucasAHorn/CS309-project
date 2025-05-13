package com.springboot.EventApp.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.dto.msgRemovalRequest;
import com.springboot.EventApp.model.entities.GroupMessages;
import com.springboot.EventApp.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.springboot.EventApp.util.UserUtil.isUserBannedFromChat;
import static com.springboot.EventApp.util.UserUtil.userIsAdminOrModerator;

/**
 * This is used to handle group chats (in a websocket)
 *
 * @author Lucas Horn
 */
@Component
public class GroupWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, List<WebSocketSession>> groupSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Autowired
    private UserInfoRepository userRepo;

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMessagesRepository groupMessagesRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;


    // This helps the serialization (gives errors without)
    public GroupWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String path = session.getUri().getPath(); // e.g., /ws/34
            String query = session.getUri().getQuery(); // e.g., userId=12

            Long groupId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

            if (query == null) {
                System.out.println("Missing query string.");
                session.close();
                return;
            }

            Map<String, String> queryParams = Arrays.stream(query.split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

            String userIdStr = queryParams.get("userId");
            if (userIdStr == null) {
                System.out.println("Missing userId in query");
                session.close();
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            if (!groupRepo.existsById(groupId)) {
                System.out.println("Group does not exist: " + groupId);
                session.close();
                return;
            }

            if (!enrollmentRepository.existsById(new EnrollmentId(userId, groupId))) {
                System.out.println("User " + userId + " is not enrolled in group " + groupId);
                session.close();
                return;
            }

            if (bannedUsersRepository.existsById(new EnrollmentId(userId, groupId))) {
                System.out.println("User is banned from chat: " + userId);
                session.sendMessage(new TextMessage("User is banned from chat - although they can view messages"));
            }

            synchronized (groupSessions) {
                groupSessions.computeIfAbsent(groupId, k -> new ArrayList<>()).add(session);
                System.out.println("WebSocket connection established for user " + userId + " in group " + groupId);
            }

        } catch (Exception e) {
            System.out.println("Error in afterConnectionEstablished: " + e.getMessage());
            try {
                session.close();
            } catch (Exception closeEx) {
                System.out.println("Error closing session: " + closeEx.getMessage());
            }
        }
    }


    @Transactional
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        Iterator<String> keyIterator = jsonNode.fieldNames();
        ArrayList<String> msgKeys = new ArrayList<>();
        while (keyIterator.hasNext()) {
            msgKeys.add(keyIterator.next());
        }

        if (msgKeys.contains("userId") && msgKeys.contains("groupId") && msgKeys.contains("message")) {

            Long userId = jsonNode.get("userId").asLong();
            Long groupId = jsonNode.get("groupId").asLong();
            String text = jsonNode.get("message").asText();

            if (!enrollmentRepository.existsById(new EnrollmentId(userId, groupId))) {
                session.sendMessage(new TextMessage("user " + userId + " not in group " + groupId));
                return;
            }

            if (isUserBannedFromChat(userId, groupId, bannedUsersRepository)) {
                session.sendMessage(new TextMessage("user " + userId + " is banned in group " + groupId + ", they" +
                        " you can still see chat though"));
                return;
            }

            GroupMessages groupMessage = new GroupMessages(text, userId, groupId, userRepo, groupRepo);
            groupMessage.setMsgTime(LocalDateTime.now());

            groupMessagesRepo.save(groupMessage);

            String broadcastJson = objectMapper.writeValueAsString(groupMessage);
            List<WebSocketSession> sessions = groupSessions.get(groupId);

            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(broadcastJson));
                    }
                }
            }
        } else if (msgKeys.contains("removeMsgId") && msgKeys.contains("removeMsg") && msgKeys.contains("managerId")) {

            Long managerId = jsonNode.get("managerId").asLong();
            Long msgId = jsonNode.get("removeMsgId").asLong();

            Optional<GroupMessages> messageOpt = groupMessagesRepo.findById(msgId);
            if (messageOpt.isEmpty()) {
                session.sendMessage(new TextMessage("message: " + msgId + " does not exist"));
                return;
            }
            GroupMessages txtMessage = messageOpt.get();
            Long groupId = txtMessage.getGroupId();

            GroupMessages dbMessage = messageOpt.get();
            if (!userIsAdminOrModerator(managerId, groupId, userRepo, enrollmentRepository, groupRepo)) {
                session.sendMessage(new TextMessage("user: " + managerId + " is not manager in group " + groupId));
                return;
            }

            groupMessagesRepo.deleteById(msgId);

            String broadcastJson = objectMapper.writeValueAsString(new msgRemovalRequest(msgId));
            List<WebSocketSession> sessions = groupSessions.get(groupId);

            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(broadcastJson));
                    }
                }
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        groupSessions.values().forEach(sessions -> sessions.remove(session));
    }
}