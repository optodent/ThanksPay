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

    /**
     * Finds account by identifier or throws {@link AccountNotFoundException} if not found.
     *
     * @param id identifier of the account
     * @return the found {@link Account}
     */
    public Account findAccountById(long id) {
        Optional<Account> account = accountRepository.findById(id);
        return account.orElseThrow(() -> new AccountNotFoundException("Account not found with provided id"));
    }

    /**
     * Creates {@link Account} with the passed account information and persists it.
     *
     * @param accountRequestDto account data to be persisted
     * @return the saved {@link Account}
     */
    public Account saveAccount(AccountRequestDto accountRequestDto) {
        return saveAccount(new Account(accountRequestDto.getBalance(), accountRequestDto.getCurrency()));
    }

    /**
     * Persists the passed account.
     *
     * @param account to be saved
     * @return the saved {@link Account}
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
}
