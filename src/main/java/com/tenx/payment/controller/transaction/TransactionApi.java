package com.tenx.payment.controller.transaction;

import com.tenx.payment.controller.BaseApi;
import com.tenx.payment.dto.transaction.TransactionRequestDto;
import com.tenx.payment.dto.transaction.TransactionResponseDto;
import com.tenx.payment.model.Transaction;
import com.tenx.payment.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionApi extends BaseApi {

    private final TransactionService transactionService;

    @Autowired
    public TransactionApi(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    public TransactionResponseDto createTransaction(@Valid @RequestBody TransactionRequestDto transactionRequestDto) {
        transactionService.assertValidTransaction(transactionRequestDto);
        Transaction transaction = transactionService.execute(transactionRequestDto);

        // Manual mapper, for prod usages use mapper like mapStruct
        return new TransactionResponseDto(
                transaction.getAmount(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getCurrency(),
                transaction.getId());
    }
}
