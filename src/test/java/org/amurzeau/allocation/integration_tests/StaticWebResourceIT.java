package org.amurzeau.allocation.integration_tests;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
public class StaticWebResourceIT {

    @Test
    public void testIndexHtml() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(Matchers.containsString("<app-root>"));
    }
}
