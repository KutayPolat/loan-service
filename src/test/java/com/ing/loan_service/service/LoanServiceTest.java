package com.ing.loan_service.service;

import com.ing.loan_service.dto.CreateLoanRequest;
import com.ing.loan_service.dto.InstallmentsResponse;
import com.ing.loan_service.dto.LoanResponse;
import com.ing.loan_service.dto.PaymentResult;
import com.ing.loan_service.exception.InsufficientCreditException;
import com.ing.loan_service.model.Customer;
import com.ing.loan_service.model.Loan;
import com.ing.loan_service.model.LoanInstallment;
import com.ing.loan_service.repository.CustomerRepository;
import com.ing.loan_service.repository.LoanInstallmentRepository;
import com.ing.loan_service.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @InjectMocks
    private LoanService loanService;

    private Customer testCustomer;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("John", "Doe", new BigDecimal("50000.00"));
        testCustomer.setId(1L);

        testLoan = new Loan(testCustomer, new BigDecimal("11000.00"), 12);
        testLoan.setId(1L);
    }

    @Test
    void createLoan_Success() {
        // Given
        CreateLoanRequest request = new CreateLoanRequest(1L, new BigDecimal("10000.00"), 0.1, "12");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(installmentRepository.saveAll(any())).thenReturn(new ArrayList<>());

        // When
        LoanResponse result = loanService.createLoan(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("11000.00"), result.getLoanAmount());
        assertEquals(12, result.getNumberOfInstallment());

        verify(customerRepository).save(testCustomer);
        verify(loanRepository).save(any(Loan.class));
        verify(installmentRepository).saveAll(any());
    }

    @Test
    void createLoan_InsufficientCredit() {
        // Given
        CreateLoanRequest request = new CreateLoanRequest(1L, new BigDecimal("100000.00"), 0.1, "12");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When & Then
        assertThrows(InsufficientCreditException.class, () -> {
            loanService.createLoan(request);
        });
    }

    @Test
    void payLoan_Success() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("2000.00");

        List<LoanInstallment> unpaidInstallments = new ArrayList<>();
        LoanInstallment installment1 = new LoanInstallment(testLoan, new BigDecimal("916.67"),
                LocalDate.now().plusDays(10));
        LoanInstallment installment2 = new LoanInstallment(testLoan, new BigDecimal("916.67"),
                LocalDate.now().plusDays(40));
        unpaidInstallments.add(installment1);
        unpaidInstallments.add(installment2);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(installmentRepository.findUnpaidByLoanIdOrderByDueDate(loanId))
                .thenReturn(unpaidInstallments);
        when(installmentRepository.save(any(LoanInstallment.class)))
                .thenReturn(new LoanInstallment());

        // When
        PaymentResult result = loanService.payLoan(loanId, paymentAmount);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getInstallmentsPaid());
        assertTrue(result.getTotalAmountSpent().compareTo(BigDecimal.ZERO) > 0);

        verify(installmentRepository, times(2)).save(any(LoanInstallment.class));
    }

    @Test
    void listLoans_Success() {
        // Given
        Long customerId = 1L;
        List<LoanInstallment> installments = new ArrayList<>();
        testLoan.setInstallments(installments);
        List<Loan> expectedLoans = List.of(testLoan);

        when(loanRepository.findLoansWithFilters(customerId, null, null))
                .thenReturn(expectedLoans);

        // When
        List<LoanResponse> result = loanService.listLoans(customerId, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listInstallments_Success() {
        // Given
        Long loanId = 1L;
        List<LoanInstallment> expectedInstallments = new ArrayList<>();
        LoanInstallment installment = new LoanInstallment(testLoan, new BigDecimal("916.67"),
                LocalDate.now().plusDays(10));
        expectedInstallments.add(installment);

        when(installmentRepository.findByLoanIdOrderByDueDate(loanId))
                .thenReturn(expectedInstallments);

        // When
        List<InstallmentsResponse> result = loanService.listInstallments(loanId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(installment.getId(), result.get(0).getInstallmentId());
    }
}
