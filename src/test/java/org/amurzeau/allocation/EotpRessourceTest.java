package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class EotpRessourceTest {

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testPutNewItemEndpoint() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Application X for Mr X\", \"isDisabled\": true }")
                .put("/eotps/123456789-0001")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo("123456789-0001"))
                .body("name", Matchers.equalTo("Application X for Mr X"))
                .body("isDisabled", Matchers.equalTo(true));

        // Test overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Application X initial development\" }")
                .put("/eotps/123456789-0001")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo("123456789-0001"))
                .body("name", Matchers.equalTo("Application X initial development"))
                .body("isDisabled", Matchers.equalTo(false));

        // Add second item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Application X maintenance\" }")
                .put("/eotps/123456789-0002")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo("123456789-0002"))
                .body("name", Matchers.equalTo("Application X maintenance"))
                .body("isDisabled", Matchers.equalTo(false));

        // Test collection contains item
        Map<String, Object> expected1 = new HashMap<String, Object>();
        expected1.put("id", "123456789-0002");
        expected1.put("name", "Application X maintenance");
        expected1.put("isDisabled", false);

        Map<String, Object> expected2 = new HashMap<String, Object>();
        expected2.put("id", "123456789-0001");
        expected2.put("name", "Application X initial development");
        expected2.put("isDisabled", false);

        given()
                .when().get("/eotps")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(2))
                .body("$", Matchers.hasItem(expected1))
                .body("$", Matchers.hasItem(expected2));
    }

    @Test
    public void testPostDeleteEndpoint() {
        int existingItemNumber = given()
                .when().get("/eotps")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$").size();

        // Add item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{ \"id\": \"9999999999-999989\", \"name\": \"To be deleted\" }")
                .post("/eotps")
                .then()
                .statusCode(201)
                .header("Location", Matchers.equalTo("/eotps/9999999999-999989"));

        given()
                .when().get("/eotps")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber + 1));

        // Do the delete
        given()
                .when().delete("/eotps/9999999999-999989")
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when().delete("/eotps/9999999999-999989")
                .then()
                .statusCode(404);

        // Check we removed one item
        given()
                .when().get("/eotps")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber));
    }
}
