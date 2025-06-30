package com.ing.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResult {

    private int installmentsPaid;
    private BigDecimal totalAmountSpent;
    private boolean loanFullyPaid;
}
