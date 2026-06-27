package com.shuu.berry.service;

import com.shuu.berry.dto.LoginRequestDTO;
import com.shuu.berry.dto.SignupRequestDTO;
import com.shuu.berry.entity.AuthProvider;
import com.shuu.berry.entity.User;
import com.shuu.berry.repository.UserRepository;
import com.shuu.berry.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  public void registerUser(SignupRequestDTO req) {
    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email is already in use");
    }

    User user = User.builder()
        .name(req.getName())
        .email(req.getEmail())
        .password(passwordEncoder.encode(req.getPassword()))
        .provider(AuthProvider.LOCAL).build();

    userRepository.save(user);
  }

  public String authenticateUser(LoginRequestDTO req) {
    User user = userRepository.findByEmail(req.getEmail()).orElse(null);

    if (user == null || user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    return jwtUtil.generateToken(user.getEmail());
  }
}
