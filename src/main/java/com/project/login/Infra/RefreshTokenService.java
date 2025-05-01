package com.project.login.Infra;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh:";

    public void save(String email, String refreshToken, long expireMs) {
        redisTemplate.opsForValue().set(PREFIX + email, refreshToken, expireMs, TimeUnit.MILLISECONDS);
    }

    public String get(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                expirationMillis,
                TimeUnit.MILLISECONDS
        );
    }

}
