package com.tenx.payment.controller.account;

import com.tenx.payment.controller.BaseApi;
import com.tenx.payment.dto.account.AccountRequestDto;
import com.tenx.payment.dto.account.AccountResponseDto;
import com.tenx.payment.model.Account;
import com.tenx.payment.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountApi extends BaseApi {

    private final AccountService accountService;

    @Autowired
    public AccountApi(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account")
    public AccountResponseDto createAccount(@Valid @RequestBody AccountRequestDto accountRequestDto) {
        return mapAccountToResponseDto(accountService.saveAccount(accountRequestDto));
    }

    @GetMapping("/account/{id}")
    public AccountResponseDto getAccount(@PathVariable long id) {
        return mapAccountToResponseDto(accountService.findAccountById(id));
    }

    private AccountResponseDto mapAccountToResponseDto(Account account) {
        return new AccountResponseDto(account.getId(), account.getBalance(), account.getCurrency(), account.getCreateAtTimestamp());
    }
}
