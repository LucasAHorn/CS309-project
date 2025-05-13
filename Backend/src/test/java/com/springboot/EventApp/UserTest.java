package com.springboot.EventApp;

import com.springboot.EventApp.model.dto.LoginRequest;
import com.springboot.EventApp.model.dto.ResetPasswordRequest;
import com.springboot.EventApp.model.entities.UserInfo;
import org.junit.jupiter.api.*;

import static com.springboot.EventApp.TestingUtil.doesUsernameExist;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * This is used to test the basic user functionality
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserTest {

    private UserInfo user = new UserInfo();
    private final String url_prefix = "http://localhost:8080/user/";



    @Test
    public void test1_CreateUser() {
        String testUsername = "user";
        int usernum = 0;

        while (doesUsernameExist(testUsername + usernum)) {
            usernum++;
        }

        user.setUsername(testUsername + usernum);
        user.setPassword("password");
        user.setFirstPetName("fluffy");
        user.setBirthCity("hometown");
        user.setEmail("email@example.com");

        String jsonString = "{\n" +
                "\"username\": \"" + user.getUsername() + "\",\n" +
                "\"password\": \"" + user.getPassword() + "\",\n" +
                "\"firstPetName\": \"" + user.getFirstPetName() + "\",\n" +
                "\"birthCity\": \"" + user.getBirthCity() + "\",\n" +
                "\"email\": \"" + user.getEmail() + "\"\n" +
                "}";

        given()
                .contentType("application/json")
                .body(jsonString)
                .when()
                .post(url_prefix + "create")
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    @Test
    public void test2_Login() {
        LoginRequest creds = new LoginRequest();
        creds.setName(user.getUsername());
        creds.setPassword(user.getPassword());

        given()
                .contentType("application/json")
                .body(creds)
                .when()
                .post(url_prefix + "login")
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    @Test
    public void test3_ChangePassword() {
        ResetPasswordRequest creds = new ResetPasswordRequest();
        creds.setUsername(user.getUsername());
        creds.setFirstPetName(user.getFirstPetName());
        creds.setBirthCity(user.getBirthCity());
        creds.setNewPassword("newPassword");
        user.setPassword("newPassword");

        given()
                .contentType("application/json")
                .body(creds)
                .when()
                .put(url_prefix + "reset-password")
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    @Test
    public void test4_deleteUser() {
        delete(url_prefix + user.getUsername() + "/" + user.getPassword())
                .then()
                .statusCode(200)
                .body("success", equalTo(1));
    }

    @Test
    public void test5_ensureUserDeleted() {
        LoginRequest creds = new LoginRequest();
        creds.setName(user.getUsername());
        creds.setPassword(user.getPassword());

        given()
                .contentType("application/json")
                .body(creds)
                .when()
                .post(url_prefix + "login")
                .then()
                .statusCode(200)
                .body("success", equalTo(0));
    }
}
