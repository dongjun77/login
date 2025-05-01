package com.project.login.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    private String result;
    private String message;
    private int status;

}
