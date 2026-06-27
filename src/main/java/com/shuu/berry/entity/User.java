package com.shuu.berry.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long id;

  @NotBlank(message = "name is required")
  private String name;

  @Column(nullable = false, unique = true)
  @NotBlank(message = "email is required")
  private String email;

  // NOTE: Json ignore dont let password get serialize and deserialize
  @JsonIgnore
  private String password;

  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  public User(String name, String email) {
    this.name = name;
    this.email = email;
  }
}
