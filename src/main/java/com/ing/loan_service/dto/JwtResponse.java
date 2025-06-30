package com.ing.loan_service.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtResponse implements Serializable {

    private final String jwttoken;
}
