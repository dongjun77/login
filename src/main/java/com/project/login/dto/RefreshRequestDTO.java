package com.project.login.dto;

import lombok.Data;

@Data
public class RefreshRequestDTO {

    private String email;
    private String refreshToken;

}
