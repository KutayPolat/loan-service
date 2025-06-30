package com.ing.loan_service.repository;

import com.ing.loan_service.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByCustomerIdAndIsPaid(Long customerId, Boolean isPaid);

    List<Loan> findByCustomerIdAndNumberOfInstallment(Long customerId, Integer numberOfInstallment);

    @Query("SELECT l FROM Loan l WHERE l.customer.id = :customerId " +
            "AND (:isPaid IS NULL OR l.isPaid = :isPaid) " +
            "AND (:numberOfInstallment IS NULL OR l.numberOfInstallment = :numberOfInstallment)")
    List<Loan> findLoansWithFilters(@Param("customerId") Long customerId,
                                    @Param("isPaid") Boolean isPaid,
                                    @Param("numberOfInstallment") Integer numberOfInstallment);
}
