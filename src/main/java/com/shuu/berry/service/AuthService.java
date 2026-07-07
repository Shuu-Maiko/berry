package com.shuu.berry.service;

import com.shuu.berry.dto.LoginRequestDTO;
import com.shuu.berry.dto.SignupRequestDTO;
import com.shuu.berry.entity.AuthProvider;
import com.shuu.berry.entity.User;
import com.shuu.berry.repository.UserRepository;
import com.shuu.berry.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

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
