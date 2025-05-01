package com.project.login.service;

import com.project.login.dto.JoinRequestDTO;
import com.project.login.dto.JoinResponseDTO;
import com.project.login.dto.LoginRequestDTO;
import com.project.login.dto.LoginResponseDTO;
import com.project.login.dto.RefreshRequestDTO;
import com.project.login.dto.RefreshResponseDTO;
import com.project.login.exception.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;

@Transactional
public interface MemberService {

    public JoinResponseDTO join(JoinRequestDTO dto);

    public LoginResponseDTO login(LoginRequestDTO dto);

    public RefreshResponseDTO refresh(String accessToken, RefreshRequestDTO dto) throws InvalidRefreshTokenException;

    void logout(String accessToken, RefreshRequestDTO dto) throws InvalidRefreshTokenException;
}
