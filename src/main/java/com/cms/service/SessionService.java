package com.cms.service;

import com.cms.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.access-token-expiry}")
    private long sessionTtl;

    private static final String SESSION_PREFIX = "cms:session:";

    public String createSession(User user, String ip, String userAgent) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId().toString());
        data.put("email", user.getEmail());
        data.put("role", user.getRole().name());
        data.put("ip", ip);
        data.put("userAgent", userAgent);
        data.put("createdAt", Instant.now().toString());

        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, Duration.ofSeconds(sessionTtl));

        log.info("Session oluşturuldu: {} — {}", sessionId, user.getEmail());
        return sessionId;
    }

    public boolean isSessionValid(String sessionId) {
        if (sessionId == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + sessionId));
    }

    public void refreshSession(String sessionId) {
        redisTemplate.expire(SESSION_PREFIX + sessionId, Duration.ofSeconds(sessionTtl));
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
        log.info("Session silindi: {}", sessionId);
    }

    public void deleteAllUserSessions(String userId) {
        Set<String> keys = redisTemplate.keys(SESSION_PREFIX + "*");
        if (keys == null) return;
        keys.stream()
            .filter(key -> userId.equals(redisTemplate.opsForHash().get(key, "userId")))
            .forEach(redisTemplate::delete);
    }
}
