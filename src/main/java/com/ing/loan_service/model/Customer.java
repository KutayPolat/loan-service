package com.ing.loan_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal usedCreditLimit = BigDecimal.ZERO;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Loan> loans;

    public Customer(String name, String surname, BigDecimal creditLimit) {
        this.name = name;
        this.surname = surname;
        this.creditLimit = creditLimit;
    }
    public BigDecimal getAvailableCredit() {
        return creditLimit.subtract(usedCreditLimit);
    }
}
