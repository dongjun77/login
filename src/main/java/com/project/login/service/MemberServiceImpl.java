package com.project.login.service;

import com.project.login.Infra.RefreshTokenService;
import com.project.login.domain.Member;
import com.project.login.domain.MemberRole;
import com.project.login.dto.JoinRequestDTO;
import com.project.login.dto.JoinResponseDTO;
import com.project.login.dto.LoginRequestDTO;
import com.project.login.dto.LoginResponseDTO;
import com.project.login.dto.RefreshRequestDTO;
import com.project.login.dto.RefreshResponseDTO;
import com.project.login.exception.EmailAlreadyUsedException;
import com.project.login.exception.InvalidRefreshTokenException;
import com.project.login.repository.MemberRepository;
import com.project.login.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public JoinResponseDTO join(JoinRequestDTO dto) {

        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .social(false)
                .nickname(dto.getNickname())
                .build();

        member.addRole(MemberRole.USER); // 가입할 때 USER 기본 권한 부여!

        memberRepository.save(member);

        return new JoinResponseDTO("success", "회원가입 성공");
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        System.out.println("비밀번호 일치");

        String accessToken = jwtUtil.generateAccessToken(member);
        String refreshToken = jwtUtil.generateRefreshToken(member);

        refreshTokenService.save(member.getEmail(), refreshToken, jwtUtil.getRefreshTokenExpireMs());

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    @Override
    public RefreshResponseDTO refresh(String accessToken, RefreshRequestDTO dto) throws InvalidRefreshTokenException {

        // 남은 시간 계산
        long exp = jwtUtil.getExpiration(accessToken);

        String storedToken = refreshTokenService.get(dto.getEmail());

        // 1. Redis에 저장된 토큰 없거나 불일치
        if (storedToken == null || !storedToken.equals(dto.getRefreshToken())) {
            throw new InvalidRefreshTokenException("RefreshToken이 유효하지 않습니다.");
        }

        // 2. 토큰 유효성 검사
        if (!jwtUtil.validateToken(dto.getRefreshToken())) {
            refreshTokenService.delete(dto.getEmail()); // 만료된 건 삭제
            throw new InvalidRefreshTokenException("RefreshToken이 만료되었습니다.");
        }

        // 3. 새 AccessToken 발급
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("이메일 없음"));

        String newAccessToken = jwtUtil.generateAccessToken(member);
        String newRefreshToken = jwtUtil.generateRefreshToken(member);

        // 기존 accessToken blacklist 등록
        refreshTokenService.blacklistAccessToken(accessToken, exp);

        // 새로운 리프레시토큰 등록
        refreshTokenService.save(member.getEmail(), newRefreshToken, jwtUtil.getRefreshTokenExpireMs());

        return new RefreshResponseDTO(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String accessToken, RefreshRequestDTO dto) throws InvalidRefreshTokenException {

        long exp = jwtUtil.getExpiration(accessToken); // 남은 시간 계산

        String storedToken = refreshTokenService.get(dto.getEmail());

        if (storedToken == null || !storedToken.equals(dto.getRefreshToken())) {
            throw new InvalidRefreshTokenException("유효하지 않은 RefreshToken입니다.");
        }

        refreshTokenService.delete(dto.getEmail());
        refreshTokenService.blacklistAccessToken(accessToken, exp);
    }
}
