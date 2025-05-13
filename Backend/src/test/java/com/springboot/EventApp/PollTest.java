//package com.springboot.EventApp;
//
//import com.springboot.EventApp.model.entities.Poll;
//import com.springboot.EventApp.model.entities.UserInfo;
//import io.restassured.response.Response;
//import org.junit.jupiter.api.*;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static com.springboot.EventApp.TestingUtil.createEventForGroup;
//import static com.springboot.EventApp.TestingUtil.deleteUser;
//import static io.restassured.RestAssured.given;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class PollTest {
//
//    static UserInfo adminUser;
//    static Long groupId;
//    static Long eventId;
//
//    @BeforeAll
//    public static void setup() {
//        // Create an admin and group
//        adminUser = TestingUtil.createUniqueTestUser("adminPoll");
//        groupId = TestingUtil.setupTestUserAndGroup(adminUser);
//
//        eventId = createEventForGroup(adminUser.getUserId(), groupId);
//    }
//
//    @AfterAll
//    public static void cleanup() {
//        deleteUser(adminUser);
//    }
//
//    @Test
//    public void test0_CreatePollReturns() {
//        Poll poll = new Poll();
//        poll.setPrompt("Favorite time?");
//        Set<String> options = new HashSet<>();
//        options.add("Morning");
//        options.add("Evening");
//        poll.setPollOptions(options);
//
//        given()
//                .contentType("application/json")
//                .body(poll)
//                .when()
//                .post("http://localhost:8080/poll/create/" + groupId + "/" + eventId + "/" + adminUser.getUserId())
//                .then()
//                .statusCode(200);
//    }
//}
