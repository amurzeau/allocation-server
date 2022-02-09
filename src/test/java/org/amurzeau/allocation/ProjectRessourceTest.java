package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
public class ProjectRessourceTest {
    @AllArgsConstructor
    public static class ProjectJsonObject {
      public String name;
      public String board;
      public String component;
      public String arch;
      public String type;
      public List<String> eotpOpen;
      public List<String> eotpClosed;
    }

    public static ProjectJsonObject[] applicationTypes = {
      new ProjectJsonObject("Project X", "PC Motherboard", "North Bridge", "x86", "firmware", List.of("1000-1"), List.of()),
      new ProjectJsonObject("Project Y", "Raspbery Pi", "BCM2835", "arm", "web-app", List.of("2000-2"), List.of("2000-1")),
      new ProjectJsonObject("Project Z", "Raspbery Pi", "BCM2835 GPU", "arm", "desktop-app", List.of("3000-3", "3000-2"), List.of("3000-1", "3000-0")),
    };

    @BeforeAll
    static public void setUp() {
      RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testPutNewItemEndpoint() {
        given()
          .when()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"Development1\",\"isDisabled\": true }")
            .put("/project/dev")
          .then()
             .statusCode(200)
             .body("id", Matchers.equalTo("dev"))
             .body("name", Matchers.equalTo("Development1"))
             .body("isDisabled", Matchers.equalTo(true));

        // Test overwriting existing value
        given()
        .when()
          .contentType(ContentType.JSON)
          .body("{\"name\": \"Development\" }")
          .put("/project/dev")
        .then()
           .statusCode(200)
           .body("id", Matchers.equalTo("dev"))
           .body("name", Matchers.equalTo("Development"))
           .body("isDisabled", Matchers.equalTo(false));

        // Add second item
        given()
        .when()
          .contentType(ContentType.JSON)
          .body("{\"name\": \"Support activities\" }")
          .put("/project/support")
        .then()
          .statusCode(200)
          .body("id", Matchers.equalTo("support"))
          .body("name", Matchers.equalTo("Support activities"))
          .body("isDisabled", Matchers.equalTo(false));

        // Test collection contains item
        Map<String, Object> expected1 = new HashMap<String, Object>();
        expected1.put("id", "dev");
        expected1.put("name", "Development");
        expected1.put("isDisabled", false);

        Map<String, Object> expected2 = new HashMap<String, Object>();
        expected2.put("id", "support");
        expected2.put("name", "Support activities");
        expected2.put("isDisabled", false);

        given()
          .when().get("/project")
          .then()
            .statusCode(200)
            .assertThat()
              .body("size()", Matchers.is(2))
              .body("$", Matchers.hasItem(expected1))
              .body("$", Matchers.hasItem(expected2));
    }
    

    @Test
    public void testPostDeleteEndpoint() {
      int existingItemNumber =
        given()
        .when().get("/project")
        .then()
          .statusCode(200)
          .extract().body().jsonPath().getList("$").size();

      // Add item
      given()
      .when()
        .contentType(ContentType.JSON)
        .body("{ \"id\": \"delete-me\", \"name\": \"To be deleted\" }")
        .post("/project")
      .then()
        .statusCode(201)
        .header("Location", Matchers.equalTo("/project/delete-me"));
        
      given()
        .when().get("/project")
        .then()
          .statusCode(200)
          .assertThat()
            .body("size()", Matchers.is(existingItemNumber + 1));

      // Do the delete
      given()
        .when().delete("/project/delete-me")
        .then()
            .statusCode(200);

      // Do the delete again, check we get a 404
      given()
        .when().delete("/project/delete-me")
        .then()
            .statusCode(404);

      // Check we removed one item
      given()
        .when().get("/project")
        .then()
          .statusCode(200)
          .assertThat()
            .body("size()", Matchers.is(existingItemNumber));
    }
}