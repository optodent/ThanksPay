package com.tenx.payment.service;

import com.tenx.payment.dto.transaction.TransactionRequestDto;
import com.tenx.payment.exception.InvalidTransactionException;
import com.tenx.payment.exception.UnsupportedCurrencyException;
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
import static org.mockito.ArgumentMatchers.any;
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
    void validateTransactionValidTransactionDtoSameCurrency() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal transactionAmountInUSD = BigDecimal.valueOf(100);
        Currency currency = Currency.getInstance("USD");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInUSD, sourceAccountId, targetAccountId, currency);

        Account sourceAccount = new Account(BigDecimal.valueOf(200), currency);
        Account targetAccount = new Account(BigDecimal.ZERO, currency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        // When
        // Then
        transactionService.assertValidTransaction(transactionRequestDto);
    }

    @Test
    void validateTransactionSameAccountsVerifyInvalidTransactionException() {
        // Given
        long accountId = 1;
        BigDecimal transactionAmountInUSD = BigDecimal.valueOf(100);
        Currency currency = Currency.getInstance("USD");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInUSD, accountId, accountId, currency);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.assertValidTransaction(transactionRequestDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Source and target account must be different");
    }

    @Test
    void validateTransactionInsufficientAmountVerifyInvalidTransactionException() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal sourceAccountBalanceInUSD = BigDecimal.valueOf(100);
        BigDecimal transactionAmountInUSD = BigDecimal.valueOf(200);
        Currency currency = Currency.getInstance("USD");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInUSD, sourceAccountId, targetAccountId, currency);

        Account sourceAccount = new Account(sourceAccountBalanceInUSD, currency);
        Account targetAccount = new Account(BigDecimal.ZERO, currency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.assertValidTransaction(transactionRequestDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient amount");
    }

    @Test
    void validateTransactionSourceAccountAndTransactionDtoCurrencyDifferent() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal transactionAmountInEUR = BigDecimal.valueOf(100);
        Currency sourceAccountCurrency = Currency.getInstance("USD");
        Currency transactionDtoCurrency = Currency.getInstance("EUR");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInEUR, sourceAccountId, targetAccountId, transactionDtoCurrency);

        Account sourceAccount = new Account(BigDecimal.valueOf(200), sourceAccountCurrency);
        Account targetAccount = new Account(BigDecimal.ZERO, sourceAccountCurrency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        BigDecimal convertedAmountInUSD = BigDecimal.valueOf(120);
        when(currencyService.convertCurrency(transactionAmountInEUR, transactionDtoCurrency, sourceAccountCurrency)).thenReturn(convertedAmountInUSD);

        // When
        transactionService.assertValidTransaction(transactionRequestDto);

        // Then
        verify(currencyService).convertCurrency(transactionAmountInEUR, transactionDtoCurrency, sourceAccountCurrency);
    }

    @Test
    void validateTransactionInsufficientAmountInSourceAccountDifferentCurrenciesVerifyInvalidTransactionException() {
        // Given
        long sourceAccountId = 1;
        long targetAccountId = 2;
        BigDecimal sourceAccountBalance = BigDecimal.valueOf(100);
        BigDecimal amountInUSD = BigDecimal.valueOf(200);
        Currency sourceAccountCurrency = Currency.getInstance("USD");
        Currency transactionDtoCurrency = Currency.getInstance("EUR");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(amountInUSD, sourceAccountId, targetAccountId, transactionDtoCurrency);

        Account sourceAccount = new Account(sourceAccountBalance, sourceAccountCurrency);
        Account targetAccount = new Account(BigDecimal.ZERO, sourceAccountCurrency);

        when(accountService.findAccountById(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.findAccountById(targetAccountId)).thenReturn(targetAccount);

        BigDecimal convertedAmount = BigDecimal.valueOf(218);
        when(currencyService.convertCurrency(amountInUSD, transactionDtoCurrency, sourceAccountCurrency)).thenReturn(convertedAmount);

        // When
        // Then
        assertThatThrownBy(() -> transactionService.assertValidTransaction(transactionRequestDto))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("Insufficient amount");
        verify(currencyService).convertCurrency(amountInUSD, transactionDtoCurrency, sourceAccountCurrency);
    }

    @Test
    void executeTransactionSupportedCurrency() {
        // Given
        Account sourceAccount = new Account(new BigDecimal("100.00"), Currency.getInstance("USD"));
        sourceAccount.setId(1L);
        when(accountService.findAccountById(sourceAccount.getId())).thenReturn(sourceAccount);

        Account targetAccount = new Account(new BigDecimal("200.00"), Currency.getInstance("EUR"));
        targetAccount.setId(2L);
        when(accountService.findAccountById(targetAccount.getId())).thenReturn(targetAccount);

        BigDecimal transactionAmountInUSD = new BigDecimal("50.00");
        Currency transactionCurrency = Currency.getInstance("USD");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInUSD, sourceAccount.getId(), targetAccount.getId(), transactionCurrency);

        when(currencyService.convertCurrency(transactionAmountInUSD, sourceAccount.getCurrency(), transactionCurrency))
                .thenReturn(new BigDecimal("50.00"));

        when(currencyService.convertCurrency(transactionAmountInUSD, transactionCurrency, targetAccount.getCurrency()))
                .thenReturn(new BigDecimal("46.00"));
        Transaction expectedTransaction = new Transaction(sourceAccount, targetAccount, transactionAmountInUSD, transactionCurrency);
        when(transactionRepository.save(any())).thenReturn(expectedTransaction);

        // When
        Transaction transaction = transactionService.execute(transactionRequestDto);

        // Then
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(targetAccount.getBalance()).isEqualByComparingTo(new BigDecimal("246.00"));
        verify(accountService).saveAccount(sourceAccount);
        verify(accountService).saveAccount(targetAccount);
        verify(transactionRepository).save(any());
    }

    @Test
    void executeTransactionUnsupportedCurrency() {
        // Given
        Account sourceAccount = new Account(new BigDecimal("100.00"), Currency.getInstance("USD"));
        sourceAccount.setId(1L);
        when(accountService.findAccountById(sourceAccount.getId())).thenReturn(sourceAccount);

        Account targetAccount = new Account(new BigDecimal("200.00"), Currency.getInstance("EUR"));
        targetAccount.setId(2L);
        when(accountService.findAccountById(targetAccount.getId())).thenReturn(targetAccount);

        BigDecimal transactionAmountInUSD = new BigDecimal("50.00");
        Currency transactionCurrency = Currency.getInstance("RUB");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto(transactionAmountInUSD, sourceAccount.getId(), targetAccount.getId(), transactionCurrency);

        String errorMessage = "Not supported currency format, only USD, EUR, GBP and BGN supported";
        when(currencyService.convertCurrency(transactionAmountInUSD, transactionCurrency, sourceAccount.getCurrency()))
                .thenThrow(new UnsupportedCurrencyException(errorMessage));

        // When
        // Then
        assertThatThrownBy(() -> transactionService.execute(transactionRequestDto))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessage(errorMessage);
    }
}