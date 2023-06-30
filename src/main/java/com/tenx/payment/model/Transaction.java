package com.tenx.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_SCALE;
import static com.tenx.payment.util.ConstantUtils.DECIMAL_DIGITS_PRECISION;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(precision = DECIMAL_DIGITS_PRECISION, scale = DECIMAL_DIGITS_SCALE)
    private BigDecimal amount;

    private Currency currency;

    public Transaction() {

    }

    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount, Currency currency) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
    }
}
