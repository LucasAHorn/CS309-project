package com.springboot.EventApp;

import com.springboot.EventApp.model.entities.UserInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.springboot.EventApp.TestingUtil.*;
import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TimeToMeetTest {

    private static UserInfo adminUser;
    private static Long groupId;

    @BeforeAll
    public static void initAdminAndGroup() {
        adminUser = createUniqueTestUser("adminUser");
        groupId = setupTestUserAndGroup(adminUser);
        assertNotNull(adminUser.getUserId(), "Admin user ID should not be null");
        assertNotNull(groupId, "Group ID should not be null");
        System.out.println("Admin ID: " + adminUser.getUserId() + ", Group ID: " + groupId);
    }

    @AfterAll
    public static void cleanupAdminUser() {
        deleteUser(adminUser);

    }

    @Test
    public void test0_GroupExists() {
        assertNotNull(groupId);
        assertNotNull(adminUser);
    }

    @Test
    public void test1_FindPotentialMeetingTimes() {
        given()
                .queryParam("startTime", "2026-05-09T13:00:00")
                .queryParam("endTime", "2026-05-09T16:00:00")
                .when()
                .get("http://localhost:8080/TimeToMeet/findPotentialEvents/30/" + groupId)
                .then()
                .statusCode(200);
    }
}
