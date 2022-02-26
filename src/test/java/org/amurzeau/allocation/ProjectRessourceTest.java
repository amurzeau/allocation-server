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
    private Map<String, String> createdRessources = new HashMap<>();

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

    public static ProjectJsonObject[] projects = {
            new ProjectJsonObject("Project X", "PC Motherboard", "North Bridge", "x86", "firmware", List.of("1000-1"),
                    List.of()),
            new ProjectJsonObject("Project Y", "Raspbery Pi", "BCM2835", "arm", "web-app",
                    List.of("2000-2", "nonexisting"),
                    List.of("2000-1")),
            new ProjectJsonObject("Project Z", "Raspbery Pi", "BCM2835 GPU", "arm", "desktop-app",
                    List.of("3000-3", "3000-2"), List.of("3000-1", "3000-0")),
    };

    private void deleteAllItems() {
        for (Map.Entry<String, String> item : createdRessources.entrySet()) {
            given()
                    .when()
                    .pathParam("endpoint", item.getKey())
                    .pathParam("id", item.getValue())
                    .delete("/{endpoint}/{id}");
        }
    }

    public void createEotp(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"id\": \"%s\", \"name\": \"%s\"}", id, name))
                .put("/eotps/" + id)
                .then()
                .statusCode(200);

        createdRessources.put("eotps", id);
    }

    public void createApplicationType(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"id\": \"%s\", \"name\": \"%s\"}", id, name))
                .put("/application-types/" + id)
                .then()
                .statusCode(200);

        createdRessources.put("application-types", id);
    }

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testProjectEndpoint() {
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

        // Post valid empty item
        Integer project1Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body("{}")
                .post("/projects")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .extract().jsonPath().get("id");

        // Get with empty items
        given()
                .when()
                .contentType(ContentType.JSON)
                .get("/projects")
                .then()
                .statusCode(200);

        // Test overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(projects[1])
                .pathParam("id", project1Id)
                .put("/projects/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(project1Id))
                .body("name", Matchers.equalTo(projects[1].name))
                .body("board", Matchers.equalTo(projects[1].board))
                .body("component", Matchers.equalTo(projects[1].component))
                .body("arch", Matchers.equalTo(projects[1].arch))
                .body("type.id", Matchers.equalTo(projects[1].type))
                .body("eotpOpen[0].id", Matchers.equalTo(projects[1].eotpOpen.get(0)))
                .body("eotpOpen", Matchers.hasSize(1))
                .body("eotpClosed[0].id", Matchers.equalTo(projects[1].eotpClosed.get(0)))
                .body("eotpClosed", Matchers.hasSize(1));

        // Get single item
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("id", project1Id)
                .get("/projects/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(project1Id))
                .body("name", Matchers.equalTo(projects[1].name))
                .body("board", Matchers.equalTo(projects[1].board))
                .body("component", Matchers.equalTo(projects[1].component))
                .body("arch", Matchers.equalTo(projects[1].arch))
                .body("type.id", Matchers.equalTo(projects[1].type))
                .body("eotpOpen[0].id", Matchers.equalTo(projects[1].eotpOpen.get(0)))
                .body("eotpOpen", Matchers.hasSize(1))
                .body("eotpClosed[0].id", Matchers.equalTo(projects[1].eotpClosed.get(0)))
                .body("eotpClosed", Matchers.hasSize(1));

        // Test delete used application
        given()
                .when()
                .pathParam("id", projects[1].type)
                .delete("/application-types/{id}")
                .then()
                .statusCode(406);

        // Test updating non existing ID
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(projects[1])
                .pathParam("id", 1000000000)
                .put("/projects/{id}")
                .then()
                .statusCode(404);

        // Add second item
        Integer project2Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body(projects[2])
                .post("/projects")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThan(project1Id))
                .body("name", Matchers.equalTo(projects[2].name))
                .extract().jsonPath().get("id");

        // Test collection contains item
        given()
                .when().get("/projects")
                .then()
                .statusCode(200)
                .assertThat()
                .body("find { it.id == %d }.name", RestAssured.withArgs(project1Id),
                        Matchers.equalTo(projects[1].name))
                .body("find { it.id == %d }.name", RestAssured.withArgs(project2Id),
                        Matchers.equalTo(projects[2].name));

        // Do the delete
        given()
                .when()
                .pathParam("id", project1Id)
                .delete("/projects/{id}")
                .then()
                .statusCode(200);
        given()
                .when()
                .pathParam("id", project2Id)
                .delete("/projects/{id}")
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when()
                .pathParam("id", project1Id)
                .delete("/projects/{id}")
                .then()
                .statusCode(404);
        given()
                .when()
                .pathParam("id", project2Id)
                .delete("/projects/{id}")
                .then()
                .statusCode(404);

        // Get non existing item
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("id", project1Id)
                .get("/projects/{id}")
                .then()
                .statusCode(404);

        deleteAllItems();
    }
}
