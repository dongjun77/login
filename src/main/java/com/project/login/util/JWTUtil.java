package com.project.login.util;

import com.project.login.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        // 시크릿 키로 서명 키 생성
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Access Token 생성
    public String generateAccessToken(Member member) {
        try {
            return Jwts.builder()
                    .setSubject(member.getEmail())
                    .claim("nickname", member.getNickname())
                    .claim("roles", member.getMemberRoleList().stream()
                            .map(Enum::name)
                            .toList())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 전체 예외 출력
            throw new RuntimeException("JWT 생성 중 오류 발생", e);
        }
    }

    // Refresh Token 생성
    public String generateRefreshToken(Member member) {
        try {
            return Jwts.builder()
                    .setSubject(member.getEmail())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 전체 예외 출력
            throw new RuntimeException("JWT 생성 중 오류 발생", e);
        }
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public long getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public long getRefreshTokenExpireMs() {
        return refreshTokenExpiration;
    }
}