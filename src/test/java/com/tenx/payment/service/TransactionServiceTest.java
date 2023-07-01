package com.tenx.payment.service;

import com.tenx.payment.dto.transaction.TransactionDto;
import com.tenx.payment.exception.InvalidTransactionException;
import com.tenx.payment.model.Account;
import com.tenx.payment.model.Transaction;
import com.tenx.payment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void prepareTransactionValidTransactionDtoSameCurrency() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(100);
        Currency currency = Currency.getInstance("USD");

        TransactionDto transactionDto = new TransactionDto(amount, sourceAccountId, targetAccountId, currency);

        Account sourceAccount = new Account(BigDecimal.valueOf(200), currency);
        Account targetAccount = new Account(BigDecimal.ZERO, currency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        // When
        Transaction transaction = transactionService.prepareTransaction(transactionDto);

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getSourceAccount()).isEqualTo(sourceAccount);
        assertThat(transaction.getTargetAccount()).isEqualTo(targetAccount);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getCurrency()).isEqualTo(currency);
    }

    @Test
    void prepareTransactionSameAccountsVerifyInvalidTransactionException() {
        // Given
        long accountId = 1;
        BigDecimal amount = BigDecimal.valueOf(100);
        Currency currency = Currency.getInstance("USD");

        TransactionDto transactionDto = new TransactionDto(amount, accountId, accountId, currency);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.prepareTransaction(transactionDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Source and target account must be different");
    }

    @Test
    void prepareTransactionInsufficientAmountVerifyInvalidTransactionException() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal sourceAccountBalance = BigDecimal.valueOf(100);
        BigDecimal amount = BigDecimal.valueOf(200);
        Currency currency = Currency.getInstance("USD");

        TransactionDto transactionDto = new TransactionDto(amount, sourceAccountId, targetAccountId, currency);

        Account sourceAccount = new Account(sourceAccountBalance, currency);
        Account targetAccount = new Account(BigDecimal.ZERO, currency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.prepareTransaction(transactionDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient amount");
    }

    @Test
    void prepareTransactionSourceAccountAndTransactionDtoCurrencyDifferent() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(100);
        Currency sourceAccountCurrency = Currency.getInstance("USD");
        Currency transactionDtoCurrency = Currency.getInstance("EUR");

        TransactionDto transactionDto = new TransactionDto(amount, sourceAccountId, targetAccountId, transactionDtoCurrency);

        Account sourceAccount = new Account(BigDecimal.valueOf(200), sourceAccountCurrency);
        Account targetAccount = new Account(BigDecimal.ZERO, sourceAccountCurrency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        BigDecimal convertedAmount = BigDecimal.valueOf(120);
        when(currencyService.convertCurrency(amount, transactionDtoCurrency, sourceAccountCurrency)).thenReturn(convertedAmount);

        // When
        Transaction transaction = transactionService.prepareTransaction(transactionDto);

        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getSourceAccount()).isEqualTo(sourceAccount);
        assertThat(transaction.getTargetAccount()).isEqualTo(targetAccount);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getCurrency()).isEqualTo(transactionDtoCurrency);
        verify(currencyService).convertCurrency(amount, transactionDtoCurrency, sourceAccountCurrency);
    }

    @Test
    void prepareTransactionInsufficientAmountInSourceAccountDifferentCurrenciesVerifyInvalidTransactionException() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal sourceAccountBalance = BigDecimal.valueOf(100);
        BigDecimal amount = BigDecimal.valueOf(200);
        Currency sourceAccountCurrency = Currency.getInstance("USD");
        Currency transactionDtoCurrency = Currency.getInstance("EUR");

        TransactionDto transactionDto = new TransactionDto(amount, sourceAccountId, targetAccountId, transactionDtoCurrency);

        Account sourceAccount = new Account(sourceAccountBalance, sourceAccountCurrency);
        Account targetAccount = new Account(BigDecimal.ZERO, sourceAccountCurrency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        BigDecimal convertedAmount = BigDecimal.valueOf(218);
        when(currencyService.convertCurrency(amount, transactionDtoCurrency, sourceAccountCurrency)).thenReturn(convertedAmount);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.prepareTransaction(transactionDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient amount");
        verify(currencyService).convertCurrency(amount, transactionDtoCurrency, sourceAccountCurrency);
    }
}