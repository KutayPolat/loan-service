package com.ing.loan_service.repository;

import com.ing.loan_service.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanIdOrderByDueDate(Long loanId);

    @Query("SELECT i FROM LoanInstallment i WHERE i.loan.id = :loanId AND i.isPaid = false ORDER BY i.dueDate")
    List<LoanInstallment> findUnpaidByLoanIdOrderByDueDate(@Param("loanId") Long loanId);
}
