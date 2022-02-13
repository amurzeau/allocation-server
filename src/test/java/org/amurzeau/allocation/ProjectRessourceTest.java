package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.util.List;

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
            new ProjectJsonObject("Project X", "PC Motherboard", "North Bridge", "x86", "firmware", List.of("1000-1"),
                    List.of()),
            new ProjectJsonObject("Project Y", "Raspbery Pi", "BCM2835", "arm", "web-app", List.of("2000-2"),
                    List.of("2000-1")),
            new ProjectJsonObject("Project Z", "Raspbery Pi", "BCM2835 GPU", "arm", "desktop-app",
                    List.of("3000-3", "3000-2"), List.of("3000-1", "3000-0")),
    };

    public void createEotp(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"name\": \"%s\"}", name))
                .put("/eotps/" + id)
                .then()
                .statusCode(200);
    }

    public void createApplicationType(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"name\": \"%s\"}", name))
                .put("/application-types/" + id)
                .then()
                .statusCode(200);
    }

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testPutNewItemEndpoint() {

        createEotp("1000-1", "EOTP for project X");

        createEotp("2000-2", "EOTP for project Y");
        createEotp("2000-1", "EOTP for project Y (obsolete)");

        createEotp("3000-0", "EOTP for project Z (obsolete)");
        createEotp("3000-1", "EOTP for project Z (obsolete 2");
        createEotp("3000-2", "EOTP for project Z variant 1");
        createEotp("3000-3", "EOTP for project Z variant 2");

        createApplicationType("firmware", "Firmware in C");
        createApplicationType("web-app", "Web application");
        createApplicationType("desktop-app", "Desktop application");

        Integer project1Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[0])
                .post("/projects")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .body("name", Matchers.equalTo("Project X"))
                .body("board", Matchers.equalTo("PC Motherboard"))
                .body("component", Matchers.equalTo("North Bridge"))
                .body("arch", Matchers.equalTo("x86"))
                .body("type.id", Matchers.equalTo("firmware"))
                .body("eotpOpen[0].id", Matchers.equalTo("1000-1"))
                .body("eotpOpen", Matchers.hasSize(1))
                .body("eotpClosed", Matchers.hasSize(0))
                .extract().jsonPath().get("id");

        // Test overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[1])
                .put("/projects/" + Long.toString(project1Id))
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(project1Id))
                .body("name", Matchers.equalTo("Project Y"))
                .body("board", Matchers.equalTo("Raspbery Pi"))
                .body("component", Matchers.equalTo("BCM2835"))
                .body("arch", Matchers.equalTo("arm"))
                .body("type.id", Matchers.equalTo("web-app"))
                .body("eotpOpen[0].id", Matchers.equalTo("2000-2"))
                .body("eotpOpen", Matchers.hasSize(1))
                .body("eotpClosed[0].id", Matchers.equalTo("2000-1"))
                .body("eotpClosed", Matchers.hasSize(1));

        // Add second item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[2])
                .post("/projects")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThan(project1Id))
                .body("name", Matchers.equalTo("Project Z"));

        // Test collection contains item
        given()
                .when().get("/projects")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(2))
                .body("[0].name", Matchers.equalTo("Project Y"))
                .body("[1].name", Matchers.equalTo("Project Z"));
    }

    @Test
    public void testPostDeleteEndpoint() {
        int existingItemNumber = given()
                .when().get("/projects")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$").size();

        // Add item
        Integer newItemToDeleteId = given()
                .when()
                .contentType(ContentType.JSON)
                .body(applicationTypes[0])
                .post("/projects")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .body("name", Matchers.equalTo("Project X"))
                .extract().jsonPath().get("id");

        given()
                .when().get("/projects")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber + 1));

        // Do the delete
        given()
                .when().delete("/projects/" + newItemToDeleteId.toString())
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when().delete("/projects/" + newItemToDeleteId.toString())
                .then()
                .statusCode(404);

        // Check we removed one item
        given()
                .when().get("/projects")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber));
    }
}
