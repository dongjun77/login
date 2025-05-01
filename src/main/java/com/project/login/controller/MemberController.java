package com.project.login.controller;

import com.project.login.dto.JoinRequestDTO;
import com.project.login.dto.JoinResponseDTO;
import com.project.login.dto.LoginRequestDTO;
import com.project.login.dto.LoginResponseDTO;
import com.project.login.dto.RefreshResponseDTO;
import com.project.login.dto.RefreshRequestDTO;
import com.project.login.dto.api.ApiResponse;
import com.project.login.exception.InvalidRefreshTokenException;
import com.project.login.service.MemberService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinResponseDTO>> join(@RequestBody @Valid JoinRequestDTO dto) {
        log.info("join----" + dto);

        JoinResponseDTO response = memberService.join(dto);

        return ResponseEntity.ok(
                ApiResponse.<JoinResponseDTO>builder()
                        .result("success")
                        .message("회원가입 성공")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO dto) {
        log.info("login----" + dto);

        LoginResponseDTO response = memberService.login(dto);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponseDTO>builder()
                        .result("success")
                        .message("로그인 성공")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .result("success")
                        .message("현재 사용자 정보 조회 성공")
                        .data(authentication.getName())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<String>> adminTest(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .result("success")
                        .message("관리자 접근 성공")
                        .data("관리자 전용입니다! 사용자: " + authentication.getName())
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDTO>> refresh(@RequestHeader("Authorization") String authHeader,
                                                                   @RequestBody RefreshRequestDTO dto)
            throws InvalidRefreshTokenException {

        String accessToken = extractToken(authHeader);

        RefreshResponseDTO token = memberService.refresh(accessToken, dto);

        return ResponseEntity.ok(
                ApiResponse.<RefreshResponseDTO>builder()
                        .result("success")
                        .message("토큰 재발급 성공")
                        .data(token)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader,
                                                      @RequestBody RefreshRequestDTO dto)
            throws InvalidRefreshTokenException {

        String accessToken = extractToken(authHeader);

        memberService.logout(accessToken, dto);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .result("success")
                        .message("로그아웃 성공")
                        .data("로그아웃 되었습니다.")
                        .build()
        );
    }

    private String extractToken(String header) {
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

}
