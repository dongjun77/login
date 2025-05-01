package com.project.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshResponseDTO {

    private String accessToken;
    private String refreshToken;

}
