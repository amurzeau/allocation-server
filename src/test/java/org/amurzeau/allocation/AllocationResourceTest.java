package org.amurzeau.allocation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;

import org.amurzeau.allocation.ProjectRessourceTest.ProjectJsonObject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@QuarkusTest
public class AllocationResourceTest {
    @AllArgsConstructor
    public static class AllocationJsonObject {
        public Integer projectId;

        public String activityTypeId;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal duration;
    }

    public static AllocationJsonObject[] allocationObjects = {
            new AllocationJsonObject(0, "dev", BigDecimal.valueOf(0.25)),
            new AllocationJsonObject(1, "support", BigDecimal.valueOf(1.25)),
            new AllocationJsonObject(0, "dev", BigDecimal.valueOf(5)),
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

    public void createActivityType(String id, String name) {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(String.format("{\"name\": \"%s\"}", name))
                .put("/activity-types/" + id)
                .then()
                .statusCode(200);
    }

    public Integer createProject(String name, String applicationType, String eotp) {
        ProjectJsonObject projectJsonObject = new ProjectJsonObject(
                name,
                "Board for " + name,
                "Component of " + name,
                "x86",
                applicationType,
                List.of(eotp), List.of());

        return given()
                .when()
                .contentType(ContentType.JSON)
                .body(projectJsonObject)
                .post("/projects")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("id");
    }

    @BeforeAll
    static public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testPutNewItemEndpoint() {
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

        Integer allocation1Id = given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[0])
                .post("/allocations")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .body("project.id", Matchers.equalTo(project1Id))
                .body("duration", Matchers.equalTo(0.25f))
                .extract().jsonPath().get("id");

        // Test overwriting existing value
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[1])
                .put("/allocations/" + Long.toString(allocation1Id))
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(allocation1Id))
                .body("project.id", Matchers.equalTo(project2Id))
                .body("duration", Matchers.equalTo(1.25f));

        // Add second item
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[2])
                .post("/allocations")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThan(allocation1Id))
                .body("project.id", Matchers.equalTo(project1Id))
                .body("duration", Matchers.equalTo(5));

        // Test collection contains item
        given()
                .when().get("/allocations")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(2))
                .body("[0].project.id", Matchers.equalTo(project2Id))
                .body("[0].duration", Matchers.equalTo(1.25f))
                .body("[1].project.id", Matchers.equalTo(project1Id))
                .body("[1].duration", Matchers.equalTo(5f));
    }

    @Test
    @Order(Order.DEFAULT + 1)
    public void testPostDeleteEndpoint() {
        int existingItemNumber = given()
                .when().get("/allocations")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$").size();

        // Add item
        Integer newItemToDeleteId = given()
                .when()
                .contentType(ContentType.JSON)
                .body(allocationObjects[0])
                .post("/allocations")
                .then()
                .statusCode(200)
                .body("id", Matchers.greaterThanOrEqualTo(0))
                .body("duration", Matchers.equalTo(0.25f))
                .extract().jsonPath().get("id");

        given()
                .when().get("/allocations")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber + 1));

        // Do the delete
        given()
                .when().delete("/allocations/" + newItemToDeleteId.toString())
                .then()
                .statusCode(200);

        // Do the delete again, check we get a 404
        given()
                .when().delete("/allocations/" + newItemToDeleteId.toString())
                .then()
                .statusCode(404);

        // Check we removed one item
        given()
                .when().get("/allocations")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", Matchers.is(existingItemNumber));
    }
}
