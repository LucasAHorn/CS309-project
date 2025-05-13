package com.springboot.EventApp;

import com.springboot.EventApp.model.entities.UserInfo;
import org.junit.jupiter.api.*;

import static com.springboot.EventApp.TestingUtil.addMember;
import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class StudyBuddyTest {

    private static UserInfo admin;
    private static UserInfo member;
    private static Long groupId;

    @BeforeAll
    public static void setup() {
        admin = TestingUtil.createUniqueTestUser("admin");
        member = TestingUtil.createUniqueTestUser("member");

        groupId = TestingUtil.setupTestUserAndGroup(admin);
        addMember(groupId, admin.getUserId(), member.getUsername());
    }

    @AfterAll
    public static void cleanup() {
        TestingUtil.deleteUser(admin);
        TestingUtil.deleteUser(member);
    }

    @Test
    public void test0_StudyBuddy() {
        given()
                .when()
                .get("http://localhost:8080/studyBuddy/find/" + admin.getUserId())
                .then()
                .statusCode(200);
    }
}