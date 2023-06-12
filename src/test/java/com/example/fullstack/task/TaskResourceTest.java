package com.example.fullstack.task;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.fullstack.user.User;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.vertx.VertxContextSupport;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TaskResourceTest {

  @BeforeEach
  void setUp() {
    RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    RestAssured.defaultParser = Parser.JSON;
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void list() throws Throwable {
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
        .statusCode(200)
        .body(
            "$",
            allOf(
                hasItem(hasEntry("title", "to-be-listed")),
                everyItem(hasEntry(is("user"), (Matcher) hasEntry("name", "user")))));
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void create() {
    given()
        .body("{\"title\":\"task-create\"}")
        .contentType(ContentType.JSON)
        .when()
        .post("/api/v1/tasks")
        .then()
        .statusCode(201)
        .body("title", is("task-create"), "created", not(emptyString()));
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void update() {
    var toUpdate =
        given()
            .body("{\"title\":\"to-update\"}")
            .contentType(ContentType.JSON)
            .post("/api/v1/tasks")
            .as(Task.class);
    toUpdate.title = "updated";
    given()
        .body(toUpdate)
        .contentType(ContentType.JSON)
        .when()
        .put("/api/v1/tasks/" + toUpdate.id)
        .then()
        .statusCode(200)
        .body("title", is("updated"), "version", is(toUpdate.version + 1));
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void updateNotFound() {
    given()
        .body("{\"title\":\"updated\"}")
        .contentType(ContentType.JSON)
        .when()
        .put("/api/v1/tasks/1337")
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void updateForbidden() throws Throwable {
    final User admin =
        VertxContextSupport.subscribeAndAwait(
            () -> Panache.withSession(() -> User.<User>findById(0L)));
    Task adminTask =
        VertxContextSupport.subscribeAndAwait(
            () ->
                Panache.withTransaction(
                    () -> {
                      Task task = new Task();
                      task.title = "admins-task";
                      task.user = admin;
                      return task.persistAndFlush();
                    }));
    given()
        .body("{\"title\":\"to-update\"}")
        .contentType(ContentType.JSON)
        .when()
        .put("/api/v1/tasks/" + adminTask.id)
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void delete() throws Throwable {
    var toDelete =
        given()
            .body("{\"title\":\"to-delete\"}")
            .contentType(ContentType.JSON)
            .post("/api/v1/tasks")
            .as(Task.class);
    given().when().delete("/api/v1/tasks/" + toDelete.id).then().statusCode(204);
    Task deleted =
        VertxContextSupport.subscribeAndAwait(
            () -> Panache.withSession(() -> Task.findById(toDelete.id)));
    assertThat(deleted, nullValue());
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  void setComplete() throws Throwable {
    var toSetComplete =
        given()
            .body("{\"title\":\"to-set-complete\"}")
            .contentType(ContentType.JSON)
            .post("/api/v1/tasks")
            .as(Task.class);
    given()
        .body("\"true\"")
        .contentType(ContentType.JSON)
        .when()
        .put("/api/v1/tasks/" + toSetComplete.id + "/complete")
        .then()
        .statusCode(200);
    Task completed =
        VertxContextSupport.subscribeAndAwait(
            () -> Panache.withSession(() -> Task.findById(toSetComplete.id)));
    assertThat(
        completed,
        allOf(
            hasProperty("complete", notNullValue()),
            hasProperty("version", is(toSetComplete.version + 1))));
  }
}
