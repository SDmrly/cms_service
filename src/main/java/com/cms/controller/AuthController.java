package com.cms.controller;

import com.cms.dto.AuthUserDto;
import com.cms.dto.LoginRequest;
import com.cms.dto.LoginResponse;
import com.cms.dto.RegisterRequest;
import com.cms.repository.UserRepository;
import com.cms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${jwt.access-token-expiry:3600}")
    private int accessTokenExpiry;

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request, httpRequest);

        // Set HttpOnly cookie
        Cookie cookie = buildCookie("access_token", response.getAccessToken(), accessTokenExpiry);
        httpResponse.addCookie(cookie);

        // Don't expose the raw token in the body — clear it for the response body
        response.setAccessToken(null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthUserDto> register(@RequestBody @Valid RegisterRequest request) {
        AuthUserDto user = authService.register(request);
        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                       HttpServletResponse response) {
        String token = extractTokenFromCookie(request);
        if (token != null) {
            authService.logout(token, request);
        }
        // Clear the cookie
        Cookie cookie = buildCookie("access_token", "", 0);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user info")
    public ResponseEntity<AuthUserDto> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String userId = (String) authentication.getPrincipal();
        return userRepository.findById(UUID.fromString(userId))
                .map(user -> ResponseEntity.ok(new AuthUserDto(
                        user.getId(), user.getEmail(), user.getRole().name())))
                .orElse(ResponseEntity.status(401).build());
    }

    // ---------------------------------------------------------------- helpers

    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        // SameSite=Strict via response header (Servlet API <6.1 doesn't expose it directly)
        return cookie;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
