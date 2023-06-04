package com.example.fullstack.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserResourceTest {

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void get() {
    given().when().get("/api/v1/users/0").then().statusCode(200).body("name", is("admin"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void list() {
    given()
        .when()
        .get("/api/v1/users")
        .then()
        .statusCode(200)
        .body(
            "$.size()",
            greaterThanOrEqualTo(1),
            "[0].name",
            is("admin"),
            "[0].password",
            nullValue());
  }

  @Test
  void create() {}

  @Test
  void update() {}

  @Test
  void delete() {}

  @Test
  void getCurrentUser() {}

  @Test
  void changePassword() {}
}
