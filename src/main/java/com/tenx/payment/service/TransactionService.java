package com.tenx.payment.service;

import com.tenx.payment.dto.transaction.TransactionDto;
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

    public Transaction prepareTransaction(TransactionDto transactionDto) {
        if (transactionDto.getSourceAccountId().equals(transactionDto.getTargetAccountId())) {
            throw new InvalidTransactionException("Source and target account must be different");
        }

        Account sourceAccount = accountService.findAccountById(transactionDto.getSourceAccountId());
        Account targetAccount = accountService.findAccountById(transactionDto.getTargetAccountId());

        // Check if the amount in the currency we are transferring is available in source account
        BigDecimal transactionAmount = transactionDto.getAmount();
        Currency transactionCurrency = transactionDto.getCurrency();
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

        return new Transaction(sourceAccount, targetAccount, transactionAmount, transactionCurrency);
    }

    @Transactional
    @Retryable(retryFor = OptimisticLockException.class)
    public void execute(Transaction transaction) {
        Account sourceAccount = transaction.getSourceAccount();
        Account targetAccount = transaction.getTargetAccount();
        BigDecimal transactionAmount = transaction.getAmount();
        Currency transactionCurrency = transaction.getCurrency();

        // Convert transactionAmount with the source account currency
        BigDecimal sourceAccountAmount = currencyService.convertCurrency(transactionAmount, transaction.getCurrency(), sourceAccount.getCurrency());

        // Subtract the amount from the source account and persist
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(sourceAccountAmount));
        accountService.saveAccount(sourceAccount);

        // Convert transactionAmount with the target account currency
        BigDecimal targetAccountAmount = currencyService.convertCurrency(transactionAmount, transaction.getCurrency(), targetAccount.getCurrency());

        // Add the transferred amount in currency format of the account and persist
        targetAccount.setBalance(targetAccount.getBalance().add(targetAccountAmount));
        accountService.saveAccount(targetAccount);

        // Persist the transaction entity
        transactionRepository.save(new Transaction(sourceAccount, targetAccount, transactionAmount, transactionCurrency));
    }
}
