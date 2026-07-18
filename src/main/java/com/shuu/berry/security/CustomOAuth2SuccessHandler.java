package com.shuu.berry.security;

import com.shuu.berry.entity.AuthProvider;
import com.shuu.berry.entity.User;
import com.shuu.berry.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import org.springframework.http.ResponseCookie;

@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Value("${app.env:prod}")
  private String appEnv;

  @Value("${app.frontend-redirect-url:http://localhost:3000/oauth2/redirect}")
  private String frontendRedirectUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    // NOTE: https://developers.google.com/identity/account-linking/oauth-linking
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    if (email == null || email.isBlank()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from OAuth2 provider");
      return;
    }

    User user = userRepository.findByEmail(email).orElseGet(() -> {
      User newUser = new User();
      newUser.setName(name != null ? name : email.split("@")[0]);
      newUser.setEmail(email);
      newUser.setProvider(AuthProvider.GOOGLE);
      return userRepository.save(newUser);
    });

    String token = jwtUtil.generateToken(user.getEmail());
    boolean isSecure = !"dev".equalsIgnoreCase(appEnv);

    ResponseCookie cookie = ResponseCookie.from("auth_token", token)
        .httpOnly(true)
        .secure(isSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(86400)
        .build();

    response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

    getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
  }
}
