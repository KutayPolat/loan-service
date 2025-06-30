package com.ing.loan_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long customerId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long loadId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal loanAmount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer numberOfInstallment;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long numberOfPaidInstallment;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
}
