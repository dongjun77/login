package com.project.login.Infra;

import com.project.login.exception.AlreadyLoggedInException;
import java.time.Duration;
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

    public void save(String email, String refreshToken, long ttlMillis) {
        // 이미 존재하면 덮어쓰지 않고 예외 던짐
        Boolean saved = redisTemplate.opsForValue()
                .setIfAbsent("refresh:" + email, refreshToken, Duration.ofMillis(ttlMillis));

        if (Boolean.FALSE.equals(saved)) {
            throw new AlreadyLoggedInException("이미 로그인되어 있는 계정입니다.");
        }
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
