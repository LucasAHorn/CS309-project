package com.springboot.EventApp;

import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.UserInfo;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.springboot.EventApp.TestingUtil.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is used to test the group functionality
 *
 * @author Lucas Horn
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GroupTest {

    private UserInfo adminUser;
    private UserInfo modUser;
    private UserInfo memberUser;

    private static Long groupId;
    private static Long subGroupId;

    @BeforeAll
    public void setupUsers() {
        adminUser = createUniqueTestUser("admin");
        modUser = createUniqueTestUser("moderator");
        memberUser = createUniqueTestUser("member");
    }

    @AfterAll
    public void cleanupUsers() {
        deleteUser(adminUser);
        deleteUser(modUser);
        deleteUser(memberUser);
    }


    @Test
    public void test00_createGroup() {

        EventGroup group = new EventGroup();
        group.setDescription("described");
        group.setTitle("title");

        given()
                .contentType("application/json")
                .body(group)
                .when()
                .post("http://localhost:8080/group/create/" + adminUser.getUserId())
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    @Test
    public void test01_getAllGroupsForUser() {
        Response response = given()
                .when()
                .get("http://localhost:8080/group/allGroups/" + adminUser.getUserId());

        response.then()
                .statusCode(200);

        List<Map<String, Object>> groups = response.jsonPath().getList("$");
        assertFalse(groups.isEmpty(), "Expected at least one group");

        groupId = (Long) ((Integer) groups.get(0).get("groupId")).longValue();
        assertNotNull(groupId, "Group ID should not be null");

        System.out.println("Group ID: " + groupId);
    }

    @Test
    public void test02_GetGroupDetails() {
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId)
                .then()
                .statusCode(200);
    }

    @Test
    public void test03_AddMember() {
        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + adminUser.getUserId() + "/addMember/" + memberUser.getUsername())
                .then()
                .statusCode(200);
    }

    @Test
    public void test04_AddModerator() {
        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + adminUser.getUserId() + "/addMember/" + modUser.getUsername())
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + adminUser.getUserId() + "/makeModerator/" + modUser.getUsername())
                .then()
                .statusCode(200);
    }

    @Test
    public void test05_GetUserLevel() {
//        admin
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/userLevel/" + adminUser.getUserId())
                .then()
                .statusCode(200)
                .body("$", equalTo(2));

//        moderator
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/userLevel/" + modUser.getUserId())
                .then()
                .statusCode(200)
                .body("$", equalTo(1));

//        member
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/userLevel/" + memberUser.getUserId())
                .then()
                .statusCode(200)
                .body("$", equalTo(0));
    }


    @Test
    public void test06_CreateSubgroup() {

        EventGroup subgroup = new EventGroup();
        subgroup.setTitle("subgroup");
        subgroup.setDescription("describing bro");

        given()
                .contentType("application/json")
                .body(subgroup)
                .when()
                .post("http://localhost:8080/group/" + groupId + "/create/" + modUser.getUserId())
                .then()
                .statusCode(200);
    }

    @Test
    public void test07_GetSubgroupsAndParentGroup() {

        Response response = given()
                .when()
                .get("http://localhost:8080/group/" + groupId + "/subgroups");

        response.then()
                .statusCode(200);

        List<Map<String, Object>> groups = response.jsonPath().getList("$");
        assertFalse(groups.isEmpty(), "Expected at least one group");

        subGroupId = (Long) ((Integer) groups.get(0).get("groupId")).longValue();
        assertNotNull(subGroupId, "Group ID should not be null");

        System.out.println("subGroup ID: " + subGroupId);

        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/parentGroup")
                .then()
                .statusCode(200);
    }

    @Test
    public void test08_DeleteMember() {
        given()
                .contentType("application/json")
                .when()
                .delete("http://localhost:8080/group/" + groupId + "/" + modUser.getUserId() + "/deleteMember/" + memberUser.getUsername())
                .then()
                .statusCode(200);
    }

    @Test
    public void test09_GetAllGroupMembers() {
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/allEnrollments")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.greaterThan(0));
    }


    @Test
    public void test10_MakeModeratorAndGiveAdmin() {
        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + modUser.getUserId() + "/makeMember/" + modUser.getUsername())
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .when()
                .put("http://localhost:8080/group/" + groupId + "/" + adminUser.getUserId() + "/giveAdmin/" + memberUser.getUsername())
                .then()
                .statusCode(200);
    }

    @Test
    public void test11_GetUserGroupInfo() {
        given()
                .contentType("application/json")
                .when()
                .get("http://localhost:8080/group/" + groupId + "/" + adminUser.getUserId() + "/history/" + modUser.getUsername())
                .then()
                .statusCode(200);
    }




    @Test
    public void test99_deleteGroup() {
        given()
                .contentType("application/json")
                .when()
                .delete("http://localhost:8080/group/" + subGroupId + "/" + modUser.getUserId())
                .then()
                .statusCode(200);
    }
}
