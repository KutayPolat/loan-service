package com.ing.loan_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer numberOfInstallment;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @Column(nullable = false)
    private Boolean isPaid = false;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LoanInstallment> installments;

    public Loan(Customer customer, BigDecimal loanAmount, Integer numberOfInstallment) {
        this.customer = customer;
        this.loanAmount = loanAmount;
        this.numberOfInstallment = numberOfInstallment;
        this.createDate = LocalDateTime.now();
    }

    public Loan(Customer customer, BigDecimal loanAmount, Integer numberOfInstallment, List<LoanInstallment> installments) {
        this.customer = customer;
        this.loanAmount = loanAmount;
        this.numberOfInstallment = numberOfInstallment;
        this.createDate = LocalDate.now().atStartOfDay();
        this.isPaid = false;
        this.installments = installments;
    }
}
