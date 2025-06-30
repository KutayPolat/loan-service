package com.ing.loan_service.exception;

public class PaymentRestrictionException extends RuntimeException {
    public PaymentRestrictionException(String message) {
        super(message);
    }
}
