package com.example.fullstack.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@UserDefinition
public class User extends PanacheEntity {

  @Username
  @Column(unique = true, nullable = false)
  public String name;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  public ZonedDateTime created;

  @Version public int version;

  @Roles
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "id"))
  @Column(name = "role")
  public List<String> roles;

  @Password
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = false)
  public String password;
}
