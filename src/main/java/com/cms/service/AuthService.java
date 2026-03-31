package com.cms.service;

import com.cms.domain.AuditLog;
import com.cms.domain.Role;
import com.cms.domain.User;
import com.cms.dto.AuthUserDto;
import com.cms.dto.LoginRequest;
import com.cms.dto.LoginResponse;
import com.cms.dto.RegisterRequest;
import com.cms.exception.AuthException;
import com.cms.repository.AuditLogRepository;
import com.cms.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout-duration:900}")
    private long lockoutDuration;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        String ip = extractIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    passwordEncoder.encode("dummy-timing-protection");
                    saveAuditLog(null, "LOGIN_FAIL_USER_NOT_FOUND", ip, userAgent);
                    return new AuthException("INVALID_CREDENTIALS", "Invalid email or password.");
                });

        if (!user.isAccountNonLocked()) {
            saveAuditLog(user.getId(), "LOGIN_FAIL_ACCOUNT_LOCKED", ip, userAgent);
            throw new AuthException("ACCOUNT_LOCKED", "Account is locked until: " + user.getLockedUntil());
        }

        if (!user.isEnabled()) {
            throw new AuthException("ACCOUNT_DISABLED", "Account is disabled.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedAttempt(user);
            saveAuditLog(user.getId(), "LOGIN_FAIL_BAD_PASSWORD", ip, userAgent);
            throw new AuthException("INVALID_CREDENTIALS", "Invalid email or password.");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String sessionId = sessionService.createSession(user, ip, userAgent);
        String accessToken = jwtService.generateAccessToken(user, sessionId);

        saveAuditLog(user.getId(), "LOGIN_SUCCESS", ip, userAgent);

        return new LoginResponse(
                new AuthUserDto(user.getId(), user.getEmail(), user.getRole().name()),
                accessToken,
                Instant.now().plusSeconds(3600)
        );
    }

    @Transactional
    public void logout(String token, HttpServletRequest httpRequest) {
        try {
            String sessionId = jwtService.extractSessionId(token);
            String userIdStr = jwtService.extractUserId(token);
            sessionService.deleteSession(sessionId);
            
            UUID userId = null;
            if (userIdStr != null && !userIdStr.isBlank()) {
                userId = UUID.fromString(userIdStr);
            }
            saveAuditLog(userId, "LOGOUT", extractIp(httpRequest), httpRequest.getHeader("User-Agent"));
        } catch (Exception e) {
            log.warn("Error during logout (token may be invalid): {}", e.getMessage());
        }
    }

    @Transactional
    public AuthUserDto register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthException("EMAIL_EXISTS", "This email is already registered.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.EDITOR);
        user.setActive(true);
        userRepository.save(user);

        log.info("New user registered: {}", email);
        return new AuthUserDto(user.getId(), user.getEmail(), user.getRole().name());
    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(Instant.now().plusSeconds(lockoutDuration));
            log.warn("Account locked: {}", user.getEmail());
        }
        userRepository.save(user);
    }

    private void saveAuditLog(UUID userId, String action, String ip, String userAgent) {
        AuditLog logEntry = new AuditLog();
        logEntry.setUserId(userId);
        logEntry.setAction(action);
        logEntry.setIpAddress(ip);
        logEntry.setUserAgent(userAgent);
        auditLogRepository.save(logEntry);
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
