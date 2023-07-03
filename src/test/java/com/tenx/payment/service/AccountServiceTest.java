package com.tenx.payment.service;

import com.tenx.payment.dto.account.AccountRequestDto;
import com.tenx.payment.exception.AccountNotFoundException;
import com.tenx.payment.model.Account;
import com.tenx.payment.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findExistingAccountById() {
        // Given
        BigDecimal accountAmount = BigDecimal.ZERO;
        Currency accountCurrency = Currency.getInstance("BGN");
        when(accountRepository.findById(any())).thenReturn(Optional.of(new Account(accountAmount, accountCurrency)));

        // When
        long accountId = 1;
        Account account = accountService.findAccountById(accountId);

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(accountAmount);
        assertThat(account.getCurrency()).isEqualTo(accountCurrency);
        verify(accountRepository).findById(accountId);
    }

    @Test
    void findNonExistingAccountVerifyExceptionThrown() {
        // Given
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        // When
        // Then
        long accountId = 1;
        assertThatThrownBy(() -> accountService.findAccountById(accountId))
                .isExactlyInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with provided id");
        verify(accountRepository).findById(accountId);
    }

    @Test
    void saveAccountWithRequestDto() {
        // Given
        AccountRequestDto accountRequestDto = AccountRequestDto.builder()
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance("BGN"))
                .build();
        when(accountRepository.save(any())).thenReturn(new Account(accountRequestDto.getBalance(), accountRequestDto.getCurrency()));

        // When
        Account account = accountService.saveAccount(accountRequestDto);

        // Then
        assertThat(account.getCurrency()).isEqualTo(accountRequestDto.getCurrency());
        assertThat(account.getBalance()).isEqualTo(accountRequestDto.getBalance());
    }

    @Test
    void saveValidAccount() {
        // Given
        Account account = new Account(new BigDecimal(100), Currency.getInstance("BGN"));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Account result = accountService.saveAccount(account);

        // Then
        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }
}