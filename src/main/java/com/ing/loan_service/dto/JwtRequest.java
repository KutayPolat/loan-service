package com.ing.loan_service.dto;

import lombok.Data;

@Data
public class JwtRequest {

    private String username;
    private String password;
}
