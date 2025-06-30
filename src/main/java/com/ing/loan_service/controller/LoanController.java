package com.ing.loan_service.controller;

import com.ing.loan_service.dto.*;
import com.ing.loan_service.model.User;
import com.ing.loan_service.repository.UserRepository;
import com.ing.loan_service.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    public LoanController(LoanService loanService, UserRepository userRepository) {
        this.loanService = loanService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanController.isOwner(authentication, #request.customerId))")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        LoanResponse loan = loanService.createLoan(request);
        return ResponseEntity.ok(loan);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanController.isOwner(authentication, #customerId))")
    public ResponseEntity<List<LoanResponse>> listLoans(
            @RequestParam Long customerId,
            @RequestParam(required = false) Boolean isPaid,
            @RequestParam(required = false) Integer numberOfInstallments) {

        List<LoanResponse> loans = loanService.listLoans(customerId, isPaid, numberOfInstallments);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}/installments")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanController.isLoanOwner(authentication, #loanId))")
    public ResponseEntity<List<InstallmentsResponse>> listInstallments(@PathVariable Long loanId) {
        List<InstallmentsResponse> installments = loanService.listInstallments(loanId);
        if (installments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/{loanId}/pay")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanController.isLoanOwner(authentication, #loanId))")
    public ResponseEntity<PaymentResult> payLoan(@PathVariable Long loanId,
                                                 @Valid @RequestBody PayLoanRequest request) {
        PaymentResult result = loanService.payLoan(loanId, request.getAmount());
        return ResponseEntity.ok(result);
    }

    public boolean isOwner(Authentication authentication, Long customerId) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        return user != null && user.getCustomer() != null &&
                user.getCustomer().getId().equals(customerId);
    }

    public boolean isLoanOwner(Authentication authentication, Long loanId) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getCustomer() == null) {
            return false;
        }

        return user.getCustomer().getLoans().stream()
                .anyMatch(loan -> loan.getId().equals(loanId));
    }
}
