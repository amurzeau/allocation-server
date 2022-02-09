package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ApplicationTypeRessourceTest {
    @AllArgsConstructor
    public static class ApplicationTypeJsonObject {
      public String id;
      public String name;
      public Boolean isDisabled;
    }

    public static ApplicationTypeJsonObject[] applicationTypes = {
      new ApplicationTypeJsonObject("web-app1", "Web Application1", true),
      new ApplicationTypeJsonObject("web-app", "Web Application", false),
      new ApplicationTypeJsonObject("desktop-app", "Desktop Application", false),
      new ApplicationTypeJsonObject("firmware", "Firmware", false),
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
            .body(applicationTypes[0])
            .put("/application_type/web-app")
          .then()
             .statusCode(200)
             .body("id", Matchers.equalTo("web-app"))
             .body("name", Matchers.equalTo(applicationTypes[0].name))
             .body("isDisabled", Matchers.equalTo(applicationTypes[0].isDisabled));

        // Test overwriting existing value
        given()
        .when()
          .contentType(ContentType.JSON)
          .body(applicationTypes[1])
          .put("/application_type/web-app")
        .then()
           .statusCode(200)
           .body("id", Matchers.equalTo("web-app"))
           .body("name", Matchers.equalTo(applicationTypes[1].name))
           .body("isDisabled", Matchers.equalTo(applicationTypes[1].isDisabled));

        // Add second item
        given()
        .when()
          .contentType(ContentType.JSON)
          .body(applicationTypes[2])
          .put("/application_type/" + applicationTypes[2].id)
        .then()
          .statusCode(200);

        // Test collection contains items
        given()
          .when().get("/application_type")
          .then()
            .statusCode(200)
            .assertThat()
              .body("size()", Matchers.is(2))
              .body("$", Utils.hasItemFromObject(applicationTypes[1]))
              .body("$", Utils.hasItemFromObject(applicationTypes[2]));
    }
    

    @Test
    public void testPostDeleteEndpoint() {
      int existingItemNumber =
        given()
        .when().get("/application_type")
        .then()
          .statusCode(200)
          .extract().body().jsonPath().getList("$").size();

      // Add item
      given()
      .when()
        .contentType(ContentType.JSON)
        .body("{ \"id\": \"delete-me\", \"name\": \"To be deleted\" }")
        .post("/application_type")
      .then()
        .statusCode(201)
        .header("Location", Matchers.equalTo("/application_type/delete-me"));
        
      given()
        .when().get("/application_type")
        .then()
          .statusCode(200)
          .assertThat()
            .body("size()", Matchers.is(existingItemNumber + 1));

      // Do the delete
      given()
        .when().delete("/application_type/delete-me")
        .then()
            .statusCode(200);

      // Do the delete again, check we get a 404
      given()
        .when().delete("/application_type/delete-me")
        .then()
            .statusCode(404);

      // Check we removed one item
      given()
        .when().get("/application_type")
        .then()
          .statusCode(200)
          .assertThat()
            .body("size()", Matchers.is(existingItemNumber));
    }
}