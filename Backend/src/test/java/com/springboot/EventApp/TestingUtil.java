package com.springboot.EventApp;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.UserInfo;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class TestingUtil {

    private static final String URL_PREFIX = "http://localhost:8080/user/";

    /**
     * Check if a username already exists in the system.
     */
    public static boolean doesUsernameExist(String username) {
        Response response = get(URL_PREFIX + "checkName/" + username);
        response.then().statusCode(200);
        return response.jsonPath().getInt("success") == 1;
    }

    /**
     * Creates a user with a unique username and returns the created UserInfo with userId set.
     */
    public static UserInfo createUniqueTestUser(String baseUsername) {
        int userNum = 0;
        while (doesUsernameExist(baseUsername + userNum)) {
            userNum++;
        }

        String finalUsername = baseUsername + userNum;

        UserInfo user = new UserInfo();
        user.setUsername(finalUsername);
        user.setPassword("password");
        user.setFirstPetName("fluffy");
        user.setBirthCity("hometown");
        user.setEmail("email" + userNum + "@example.com");

        String jsonBody = "{\n" +
                "\"username\": \"" + user.getUsername() + "\",\n" +
                "\"password\": \"" + user.getPassword() + "\",\n" +
                "\"firstPetName\": \"" + user.getFirstPetName() + "\",\n" +
                "\"birthCity\": \"" + user.getBirthCity() + "\",\n" +
                "\"email\": \"" + user.getEmail() + "\"\n" +
                "}";

        Response response = given()
                .contentType("application/json")
                .body(jsonBody)
                .when()
                .post(URL_PREFIX + "create");

        response.then()
                .statusCode(200)
                .body("success", equalTo(1));

        Long userId = response.jsonPath().getLong("userId");
        user.setId(userId);

        return user;
    }

    /**
     * Deletes the specified user using their username and password.
     */
    public static void deleteUser(UserInfo user) {
        given()
                .when()
                .delete(URL_PREFIX + user.getUsername() + "/" + user.getPassword())
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    public static Long setupTestUserAndGroup(UserInfo user) {
        UserInfo createdUser = createUniqueTestUser(user.getUsername());

        EventGroup group = new EventGroup();
        group.setDescription("Test group description");
        group.setTitle("Test group title");

        given()
                .contentType("application/json")
                .body(group)
                .when()
                .post("http://localhost:8080/group/create/" + createdUser.getUserId())
                .then()
                .statusCode(200)
                .body("success", equalTo(1));

        Response response = given()
                .when()
                .get("http://localhost:8080/group/allGroups/" + createdUser.getUserId());

        response.then().statusCode(200);

        List<Map<String, Object>> groups = response.jsonPath().getList("$");
        if (groups.isEmpty()) {
            throw new RuntimeException("No groups found for user");
        }

        return ((Integer) groups.get(0).get("groupId")).longValue();
    }

    public static void addMember(Long groupId, Long adminUserId, String memberUsername) {
        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + adminUserId + "/addMember/" + memberUsername)
                .then()
                .statusCode(200);
    }

//    public static Long createEventForGroup(Long managerId, Long groupId) {
//        String jsonBody = "{\n" +
//                "  \"title\": \"Test Event\",\n" +
//                "  \"description\": \"This is a test event\",\n" +
//                "  \"eventTime\": \"" + LocalDateTime.now().plusDays(1).withNano(0) + "\",\n" +
//                "  \"location\": \"Test Location\",\n" +
//                "  \"capacity\": 20,\n" +
//                "  \"durationInMinutes\": 60,\n" +
//                "  \"eventGroupId\": " + groupId + "\n" +
//                "}";
//
//        given()
//                .contentType("application/json")
//                .body(jsonBody)
//                .when()
//                .post("http://localhost:8080/event/create/" + managerId)
//                .then()
//                .statusCode(200);
//
//        Response response = given()
//                .when()
//                .get("http://localhost:8080/event/group/" + groupId);
//
//        response.then().statusCode(200);
//        List<Map<String, Object>> events = response.jsonPath().getList("$");
//
//        if (events.isEmpty()) {
//            throw new RuntimeException("No events found after creation");
//        }
//
//        return ((Number) events.get(0).get("eventId")).longValue();
//    }
}
