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
public class NamedItemRessourceTest {
    @AllArgsConstructor
    public static class NamedItemJsonObject {
        public String id;
        public String name;
        public Boolean isDisabled;
    }

    public static NamedItemJsonObject[] applicationTypes = {
            new NamedItemJsonObject("test-id1", "Web Application1", null),
            new NamedItemJsonObject("test-id1", "Web Application", false),
            new NamedItemJsonObject("test-id2", "Desktop Application", true),
            new NamedItemJsonObject("test-invalid-id_=/", "Desktop Application", false),
    };

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public void testNamedItem(String restEndpoint) {
        // Post valid item with null isDisabled field
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[0])
                .pathParam("endpoint", restEndpoint)
                .post("/{endpoint}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(applicationTypes[0].id))
                .body("name", Matchers.equalTo(applicationTypes[0].name))
                .body("isDisabled", Matchers.equalTo(false));

        // Post duplicate item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[0])
                .pathParam("endpoint", restEndpoint)
                .post("/{endpoint}")
                .then()
                .statusCode(409);

        // Post with null ID
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{}")
                .pathParam("endpoint", restEndpoint)
                .post("/{endpoint}")
                .then()
                .statusCode(406);

        // Post with invalid ID characters
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[3])
                .pathParam("endpoint", restEndpoint)
                .post("/{endpoint}")
                .then()
                .statusCode(406);

        // Put with ID missmatch between body and URL path
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[1])
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", "test-invalid-id")
                .put("/{endpoint}/{id}")
                .then()
                .statusCode(406);

        // Put with null ID in body
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{}")
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", "test-invalid-id")
                .put("/{endpoint}/{id}")
                .then()
                .statusCode(406);

        // Put with invalid ID
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{}")
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", "test-invalid-id_=")
                .put("/{endpoint}/{id}")
                .then()
                .statusCode(406);

        // Test overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[1])
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", applicationTypes[1].id)
                .put("/{endpoint}/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(applicationTypes[1].id))
                .body("name", Matchers.equalTo(applicationTypes[1].name))
                .body("isDisabled", Matchers.equalTo(applicationTypes[1].isDisabled));

        // Get existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", applicationTypes[1].id)
                .get("/{endpoint}/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(applicationTypes[1].id))
                .body("name", Matchers.equalTo(applicationTypes[1].name))
                .body("isDisabled", Matchers.equalTo(applicationTypes[1].isDisabled));

        // Add second item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[2])
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", applicationTypes[2].id)
                .put("/{endpoint}/{id}")
                .then()
                .statusCode(200);

        // Test collection contains items including deleted ones
        given()
                .when()
                .pathParam("endpoint", restEndpoint)
                .queryParam("deleted", true)
                .get("/{endpoint}")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", Utils.hasItemFromObject(applicationTypes[1]))
                .body("$", Utils.hasItemFromObject(applicationTypes[2]));

        // Test collection contains items without deleted ones
        given()
                .when()
                .pathParam("endpoint", restEndpoint)
                .get("/{endpoint}")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", Utils.hasItemFromObject(applicationTypes[1]))
                .body("$", Matchers.not(Utils.hasItemFromObject(applicationTypes[2])));

        // Do the delete
        given()
                .when()
                .pathParam("id", applicationTypes[1].id)
                .pathParam("endpoint", restEndpoint)
                .delete("/{endpoint}/{id}")
                .then()
                .statusCode(200);
        given()
                .when()
                .pathParam("id", applicationTypes[2].id)
                .pathParam("endpoint", restEndpoint)
                .delete("/{endpoint}/{id}")
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when()
                .pathParam("id", applicationTypes[1].id)
                .pathParam("endpoint", restEndpoint)
                .delete("/{endpoint}/{id}")
                .then()
                .statusCode(404);
        given()
                .when()
                .pathParam("id", applicationTypes[2].id)
                .pathParam("endpoint", restEndpoint)
                .delete("/{endpoint}/{id}")
                .then()
                .statusCode(404);

        // Get non existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("endpoint", restEndpoint)
                .pathParam("id", applicationTypes[1].id)
                .get("/{endpoint}/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testActivityTypeEndpoint() {
        testNamedItem("activity-types");
    }

    @Test
    public void testApplicationTypeEndpoint() {
        testNamedItem("application-types");
    }

    @Test
    public void testEotpEndpoint() {
        testNamedItem("eotps");
    }
}
