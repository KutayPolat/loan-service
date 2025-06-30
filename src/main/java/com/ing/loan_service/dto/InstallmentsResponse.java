package com.ing.loan_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstallmentsResponse {

    private Long installmentId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate paymentDate;
    private Boolean paid;
}
