package com.example.fullstack.task;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TaskResourceTest {

  @Test
  @TestSecurity(user = "user", roles = "user")
  void list() {
    RestAssured.defaultParser = Parser.JSON;
    given()
        .body("{\"title\":\"to-be-listed\"}")
        .contentType(ContentType.JSON)
        .when()
        .post("/api/v1/tasks")
        .as(Task.class);
    given()
        .when()
        .get("/api/v1/tasks")
        .then()
        .body(
            "$",
            allOf(hasItem(hasEntry("title", "to-be-listed"))),
            everyItem(hasEntry(is("user"), hasEntry("name", "user"))));
  }

  @Test
  void create() {}

  @Test
  void update() {}

  @Test
  void updateNotFound() {}

  @Test
  void updateForbidden() {}

  @Test
  void delete() {}

  @Test
  void setComplete() {}
}
