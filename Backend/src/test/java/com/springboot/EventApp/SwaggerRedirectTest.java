package com.springboot.EventApp;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class SwaggerRedirectTest {

    @Test
    public void testRootRedirectsToSwagger() {
        given()
                .when()
                .get("http://localhost:8080/")
                .then()
                .statusCode(200);
    }
}
