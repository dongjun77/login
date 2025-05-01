package com.project.login.security;

import com.google.gson.Gson;
import com.project.login.dto.api.ApiErrorResponse;
import com.project.login.security.exception.BlacklistedTokenException;
import com.project.login.security.exception.InvalidJwtTokenException;
import com.project.login.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        if(uri.startsWith("/api/member/refresh")){
            return true;
        }
        if(uri.startsWith("/api/member/logout")){
            return true;
        }
        if(uri.startsWith("/api/member/login")){
            return true;
        }
        if(uri.startsWith("/api/member/join")){
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        try {
            // 1. Authorization 헤더에 Bearer 토큰이 없으면 그냥 다음 필터로 넘김
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                filterChain.doFilter(request, response);
//                return;
                throw new AuthenticationCredentialsNotFoundException("토큰이 없습니다.");
            }

            String token = authHeader.substring(7); // "Bearer " 제거

            // 2. 토큰 유효성 검사
            if (redisTemplate.hasKey("blacklist:" + token)) {
                log.warn("블랙리스트 토큰 접근 시도: {}", token);
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return;
                throw new BlacklistedTokenException("이미 로그아웃된 토큰입니다.");
            }

            if (!jwtUtil.validateToken(token)) {
//                filterChain.doFilter(request, response);
//                return;
                throw new InvalidJwtTokenException("잘못된 토큰입니다.");
            }

            // 3. 토큰에서 이메일 + 권한 추출
            Claims claims = jwtUtil.getClaims(token);
            String email = claims.getSubject();

            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // 4. 시큐리티 컨텍스트에 인증 객체 등록
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (BlacklistedTokenException | InvalidJwtTokenException | AuthenticationCredentialsNotFoundException e) {
            log.warn("JWT 예외 발생: {}", e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .result("fail")
                    .message(e.getMessage())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .build();

            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }


    }
}
