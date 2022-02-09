package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AllocationResourceTest {

    @Test
    public void testAllocationEndpoint() {
        given()
          .when().get("/allocation")
          .then()
             .statusCode(200);
    }

    @Test
    public void testProjectEndpoint() {
        given()
          .when().get("/project")
          .then()
             .statusCode(200);
    }

    @Test
    public void testUserEndpoint() {
        given()
          .when().get("/user")
          .then()
             .statusCode(200);
    }
}