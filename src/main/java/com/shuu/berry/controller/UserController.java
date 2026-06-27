package com.shuu.berry.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuu.berry.entity.User;
import com.shuu.berry.repository.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserRepository repository;

  public UserController(UserRepository repository) {
    this.repository = repository;
  }

  @PostMapping
  public User create(@RequestBody User user) {
    return repository.save(user);
  }

  @GetMapping
  public List<User> list() {
    return repository.findAll();
  }
}
