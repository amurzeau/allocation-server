package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;

import org.amurzeau.allocation.ProjectResourceTest.ProjectJsonObject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
public class AllocationResourceTest {
    private Map<String, String> createdRessources = new HashMap<>();

    @AllArgsConstructor
    public static class AllocationJsonObject {
        public Integer projectId;

        public String activityTypeId;

        public Float duration;
    }

    public static AllocationJsonObject[] allocationObjects = {
            new AllocationJsonObject(0, "dev", 0.25f),
            new AllocationJsonObject(1, "support", 1.25f),
            new AllocationJsonObject(0, "dev", 5f),
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

    public void createActivityType(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"id\": \"%s\", \"name\": \"%s\"}", id, name))
                .put("/activity-types/" + id)
                .then()
                .statusCode(200);

        createdRessources.put("activity-types", id);
    }

    public Integer createProject(String name, String applicationType, String eotp) {
        ProjectJsonObject projectJsonObject = new ProjectJsonObject(
                name,
                "Board for " + name,
                "Component of " + name,
                "x86",
                applicationType,
                List.of(eotp), List.of());

        Integer projectId = given()
                .when()
                .contentType(ContentType.JSON)
                .body(projectJsonObject)
                .post("/projects")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        createdRessources.put("projects", projectId.toString());

        return projectId;
    }

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testAllocationEndpoint() {
        createEotp("1000-1", "EOTP for project X");
        createEotp("2000-2", "EOTP for project Y");

        createApplicationType("firmware", "Firmware in C");
        createApplicationType("web-app", "Web application");

        Integer project1Id = createProject("Project X", "firmware", "1000-1");
        Integer project2Id = createProject("Project Y", "web-app", "2000-2");

        createActivityType("dev", "Development");
        createActivityType("support", "Support");

        allocationObjects[0].projectId = project1Id;
        allocationObjects[1].projectId = project2Id;
        allocationObjects[2].projectId = project1Id;

        // Post valid empty item
        Integer allocation1Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body("{}")
                .post("/allocations")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .extract().jsonPath().get("id");

        // Get with empty items
        given()
                .when()
                .contentType(ContentType.JSON)
                .get("/allocations")
                .then()
                .statusCode(200);

        // Put overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[1])
                .pathParam("id", allocation1Id)
                .put("/allocations/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(allocation1Id))
                .body("project.id", Matchers.equalTo(project2Id))
                .body("activityType.id", Matchers.equalTo(allocationObjects[1].activityTypeId))
                .body("duration", Matchers.equalTo(1.25f));

        // Get single item
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("id", allocation1Id)
                .get("/allocations/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(allocation1Id))
                .body("project.id", Matchers.equalTo(project2Id))
                .body("activityType.id", Matchers.equalTo(allocationObjects[1].activityTypeId))
                .body("duration", Matchers.equalTo(1.25f));

        // Test delete used project
        given()
                .when()
                .pathParam("id", project2Id)
                .delete("/projects/{id}")
                .then()
                .statusCode(406);

        // Test delete used activity
        given()
                .when()
                .pathParam("id", allocationObjects[1].activityTypeId)
                .delete("/activity-types/{id}")
                .then()
                .statusCode(406);

        // Test updating non existing ID
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[1])
                .pathParam("id", 1000000000)
                .put("/allocations/{id}")
                .then()
                .statusCode(404);

        // Add second item
        Integer allocation2Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[2])
                .post("/allocations")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThan(allocation1Id))
                .body("project.id", Matchers.equalTo(project1Id))
                .body("activityType.id", Matchers.equalTo(allocationObjects[2].activityTypeId))
                .body("duration", Matchers.equalTo(allocationObjects[2].duration))
                .extract().jsonPath().get("id");

        // Test collection contains item
        given()
                .when().get("/allocations")
                .then()
                .statusCode(200)
                .assertThat()
                .body("find { it.id == %d }.project.id", RestAssured.withArgs(allocation1Id),
                        Matchers.equalTo(allocationObjects[1].projectId))
                .body("find { it.id == %d }.activityType.id", RestAssured.withArgs(allocation1Id),
                        Matchers.equalTo(allocationObjects[1].activityTypeId))
                .body("find { it.id == %d }.duration", RestAssured.withArgs(allocation1Id),
                        Matchers.equalTo(allocationObjects[1].duration))
                .body("find { it.id == %d }.project.id", RestAssured.withArgs(allocation2Id),
                        Matchers.equalTo(allocationObjects[2].projectId))
                .body("find { it.id == %d }.activityType.id", RestAssured.withArgs(allocation2Id),
                        Matchers.equalTo(allocationObjects[2].activityTypeId))
                .body("find { it.id == %d }.duration", RestAssured.withArgs(allocation2Id),
                        Matchers.equalTo(allocationObjects[2].duration));

        // Do the delete
        given()
                .when()
                .pathParam("id", allocation1Id)
                .delete("/allocations/{id}")
                .then()
                .statusCode(200);
        given()
                .when()
                .pathParam("id", allocation2Id)
                .delete("/allocations/{id}")
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when()
                .pathParam("id", allocation1Id)
                .delete("/allocations/{id}")
                .then()
                .statusCode(404);
        given()
                .when()
                .pathParam("id", allocation2Id)
                .delete("/allocations/{id}")
                .then()
                .statusCode(404);

        // Get non existing item
        given()
                .when()
                .contentType(ContentType.JSON)
                .pathParam("id", allocation1Id)
                .get("/allocations/{id}")
                .then()
                .statusCode(404);

        deleteAllItems();
    }
}
