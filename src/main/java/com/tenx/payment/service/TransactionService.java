package com.tenx.payment.service;

import com.tenx.payment.dto.transaction.TransactionRequestDto;
import com.tenx.payment.exception.InvalidTransactionException;
import com.tenx.payment.model.Account;
import com.tenx.payment.model.Transaction;
import com.tenx.payment.repository.TransactionRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final CurrencyService currencyService;

    @Autowired
    public TransactionService(AccountService accountService, TransactionRepository transactionRepository,
                              CurrencyService currencyService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.currencyService = currencyService;
    }

    /**
     * Validates the {@link TransactionRequestDto} internal in order to make a money transaction from
     * account A to account B.
     *
     * Must be invoked before {@link TransactionService#execute(TransactionRequestDto)} in order to verify
     * the integrity of the {@link TransactionRequestDto}
     *
     * 1. Check against same account money transfers.
     * 2. Check against insufficient currency is source account.
     *
     * @param transactionRequestDto to be validated
     */
    public void assertValidTransaction(TransactionRequestDto transactionRequestDto) {
        if (transactionRequestDto.getSourceAccountId().equals(transactionRequestDto.getTargetAccountId())) {
            throw new InvalidTransactionException("Source and target account must be different");
        }

        Account sourceAccount = accountService.findAccountById(transactionRequestDto.getSourceAccountId());

        // Check if the amount in the currency we are transferring is available in source account
        BigDecimal transactionAmount = transactionRequestDto.getAmount();
        Currency transactionCurrency = transactionRequestDto.getCurrency();
        BigDecimal amountToVerify;
        if (!sourceAccount.getCurrency().equals(transactionCurrency)) {
            // Currencies are different => convert the amount in the source account currency
            amountToVerify = currencyService.convertCurrency(transactionAmount, transactionCurrency, sourceAccount.getCurrency());
        } else {
            amountToVerify = transactionAmount;
        }

        if (sourceAccount.getBalance().compareTo(amountToVerify) < 0) {
            throw new InvalidTransactionException("Insufficient amount");
        }
    }

    /**
     * Transfers {@link TransactionRequestDto#getAmount()} from a source account to a target account.
     * The amount to transfer will be debited from the source account and will credit the target account.
     * Internally supports conversion to different currencies if source and target account currency are not the same.
     *
     * The method must be executed with conjunction of {@link TransactionService#assertValidTransaction(TransactionRequestDto)}
     * to ensure the integrity of the transaction internals.
     *
     * @param transactionRequestDto to be executed and persisted
     * @return the persisted {@link Transaction}
     */
    @Transactional
    @Retryable(retryFor = {OptimisticLockException.class})
    public Transaction execute(TransactionRequestDto transactionRequestDto) {
        Account sourceAccount = accountService.findAccountById(transactionRequestDto.getSourceAccountId());
        Account targetAccount = accountService.findAccountById(transactionRequestDto.getTargetAccountId());
        BigDecimal transactionAmount = transactionRequestDto.getAmount();
        Currency transactionCurrency = transactionRequestDto.getCurrency();

        // Convert transactionAmount with the source account currency
        BigDecimal sourceAccountAmount = currencyService.convertCurrency(transactionAmount, transactionCurrency, sourceAccount.getCurrency());

        // Subtract the amount from the source account and persist
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(sourceAccountAmount));
        accountService.saveAccount(sourceAccount);

        // Convert transactionAmount with the target account currency
        BigDecimal targetAccountAmount = currencyService.convertCurrency(transactionAmount, transactionCurrency, targetAccount.getCurrency());

        // Add the transferred amount in currency format of the account and persist
        targetAccount.setBalance(targetAccount.getBalance().add(targetAccountAmount));
        accountService.saveAccount(targetAccount);

        // Persist the transaction entity
        return transactionRepository.save(new Transaction(sourceAccount, targetAccount, transactionAmount,  transactionCurrency));
    }
}
