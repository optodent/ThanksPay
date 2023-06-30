package com.tenx.payment.service;

import com.tenx.payment.dto.account.AccountRequestDto;
import com.tenx.payment.exception.AccountNotFoundException;
import com.tenx.payment.model.Account;
import com.tenx.payment.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account findAccountById(long id) {
        Optional<Account> account = accountRepository.findById(id);
        return account.orElseThrow(() -> new AccountNotFoundException("Account not found with provided id"));
    }

    public Account saveAccount(AccountRequestDto accountRequestDto) {
        return saveAccount(new Account(accountRequestDto.getBalance(), accountRequestDto.getCurrency()));
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
}
