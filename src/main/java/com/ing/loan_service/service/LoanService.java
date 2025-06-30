package com.ing.loan_service.service;

import com.ing.loan_service.dto.CreateLoanRequest;
import com.ing.loan_service.dto.InstallmentsResponse;
import com.ing.loan_service.dto.LoanResponse;
import com.ing.loan_service.dto.PaymentResult;
import com.ing.loan_service.exception.InsufficientCreditException;
import com.ing.loan_service.exception.PaymentRestrictionException;
import com.ing.loan_service.model.Customer;
import com.ing.loan_service.model.Loan;
import com.ing.loan_service.model.LoanInstallment;
import com.ing.loan_service.repository.CustomerRepository;
import com.ing.loan_service.repository.LoanInstallmentRepository;
import com.ing.loan_service.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanInstallmentRepository installmentRepository;

    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found"));

        // Calculate total loan amount with interest
        BigDecimal totalAmount = request.getAmount()
                .multiply(BigDecimal.valueOf(1 + request.getInterestRate()))
                .setScale(2, RoundingMode.HALF_UP);

        // Check credit limit
        if (customer.getAvailableCredit().compareTo(totalAmount) < 0) {
            throw new InsufficientCreditException("Insufficient credit limit");
        }

        // Create loan WITHOUT installments first
        Loan loan = new Loan(customer, totalAmount, request.getNumberOfInstallmentsAsInt());
        loan = loanRepository.save(loan);

        // Calculate installment amount
        BigDecimal installmentAmount = totalAmount
                .divide(BigDecimal.valueOf(request.getNumberOfInstallmentsAsInt()), 2, RoundingMode.HALF_UP);

        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 1; i <= request.getNumberOfInstallmentsAsInt(); i++) {
            LocalDate dueDate = currentDate.plusMonths(i).withDayOfMonth(1);
            LoanInstallment installment = new LoanInstallment(installmentAmount, dueDate);
            installment.setLoan(loan); // Set the loan reference
            installments.add(installment);
        }


        // Save installments
        installmentRepository.saveAll(installments);

        // Update customer's used credit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalAmount));
        customerRepository.save(customer);

        return LoanResponse.builder().
                customerId(customer.getId()).
                loadId(loan.getId()).
                loanAmount(loan.getLoanAmount()).
                numberOfInstallment(loan.getNumberOfInstallment()).
                build();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> listLoans(Long customerId, Boolean isPaid, Integer numberOfInstallments) {
        List<Loan> customerLoans = loanRepository.findLoansWithFilters(customerId, isPaid, numberOfInstallments);
        List<LoanResponse> loans = new ArrayList<>();
        if (customerLoans.isEmpty()) {
            loans.add(LoanResponse.builder().
                    customerId(customerId).
                    status("Customer does not have loan!").
                    build());
            return loans;
        }
        customerLoans.forEach(loan -> {
            List<LoanInstallment> installments = loan.getInstallments();
            long paidInstallmentCount = installments.isEmpty() ? loan.getNumberOfInstallment() : loan.getInstallments().stream().filter(LoanInstallment::getIsPaid).count();
            LoanResponse loanResponse = LoanResponse.builder().
                    customerId(customerId).
                    loadId(loan.getId()).
                    loanAmount(loan.getLoanAmount()).
                    numberOfInstallment(loan.getNumberOfInstallment()).
                    numberOfPaidInstallment(paidInstallmentCount).
                    status(paidInstallmentCount != loan.getNumberOfInstallment() ? "Not paid installment is available" : "Loan is paid!").
                    build();
            loans.add(loanResponse);
        });
        return loans;
    }

    @Transactional(readOnly = true)
    public List<InstallmentsResponse> listInstallments(Long loanId) {
        List<LoanInstallment> loanInstallments = installmentRepository.findByLoanIdOrderByDueDate(loanId);
        if (loanInstallments.isEmpty()) {
            return Collections.emptyList();
        }
        List<InstallmentsResponse> installments = new ArrayList<>();
        loanInstallments.forEach(loanInstallment -> {
            installments.add(InstallmentsResponse.builder()
                    .installmentId(loanInstallment.getId())
                    .amount(loanInstallment.getAmount())
                    .paidAmount(loanInstallment.getPaidAmount())
                    .dueDate(loanInstallment.getDueDate())
                    .paymentDate(loanInstallment.getPaymentDate())
                    .paid(loanInstallment.getIsPaid())
                    .build());
        });
        return installments;
    }

    @Transactional
    public PaymentResult payLoan(Long loanId, BigDecimal amount) {
        // Validate loan exists
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

        // Get unpaid installments ordered by due date
        List<LoanInstallment> unpaidInstallments = installmentRepository.findUnpaidByLoanIdOrderByDueDate(loanId);

        if (unpaidInstallments.isEmpty()) {
            throw new RuntimeException("No unpaid installments found");
        }

        LocalDate today = LocalDate.now();
        LocalDate maxPayableDate = today.plusMonths(3);

        // Filter installments that can be paid (within 3 months)
        List<LoanInstallment> payableInstallments = unpaidInstallments.stream()
                .filter(installment -> !installment.getDueDate().isAfter(maxPayableDate))
                .toList();

        if (payableInstallments.isEmpty()) {
            throw new PaymentRestrictionException("No installments can be paid within 3 months period");
        }

        BigDecimal remainingAmount = amount;
        int installmentsPaid = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (LoanInstallment installment : payableInstallments) {
            BigDecimal effectiveAmount = calculateEffectiveAmount(installment, today);

            if (remainingAmount.compareTo(effectiveAmount) >= 0) {
                // Pay this installment
                installment.setPaidAmount(effectiveAmount);
                installment.setPaymentDate(today);
                installment.setIsPaid(true);

                installmentRepository.save(installment);

                remainingAmount = remainingAmount.subtract(effectiveAmount);
                totalSpent = totalSpent.add(effectiveAmount);
                installmentsPaid++;
            } else {
                break; // Cannot pay this installment wholly
            }
        }

        if (installmentsPaid == 0) {
            throw new RuntimeException("Amount is insufficient to pay any installment");
        }

        // Check if loan is fully paid
        boolean loanFullyPaid = installmentRepository.findUnpaidByLoanIdOrderByDueDate(loanId).isEmpty();

        if (loanFullyPaid) {
            loan.setIsPaid(true);
            loanRepository.save(loan);

            // Update customer's used credit limit
            Customer customer = loan.getCustomer();
            customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(loan.getLoanAmount()));
            customerRepository.save(customer);
        }

        return new PaymentResult(installmentsPaid, totalSpent, loanFullyPaid);
    }

    private BigDecimal calculateEffectiveAmount(LoanInstallment installment, LocalDate paymentDate) {
        LocalDate dueDate = installment.getDueDate();
        BigDecimal baseAmount = installment.getAmount();

        long daysDifference = ChronoUnit.DAYS.between(dueDate, paymentDate);

        if (daysDifference < 0) {
            // Early payment - discount
            BigDecimal discount = baseAmount
                    .multiply(BigDecimal.valueOf(0.001))
                    .multiply(BigDecimal.valueOf(Math.abs(daysDifference)));
            return baseAmount.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        } else if (daysDifference > 0) {
            // Late payment - penalty
            BigDecimal penalty = baseAmount
                    .multiply(BigDecimal.valueOf(0.001))
                    .multiply(BigDecimal.valueOf(daysDifference));
            return baseAmount.add(penalty).setScale(2, RoundingMode.HALF_UP);
        } else {
            // On time payment
            return baseAmount;
        }
    }
}
