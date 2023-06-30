package com.tenx.payment.controller.transaction;

import com.tenx.payment.controller.BaseApi;
import com.tenx.payment.dto.transaction.TransactionDto;
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
    public void createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        Transaction transaction = transactionService.prepareTransaction(transactionDto);
        transactionService.execute(transaction);
    }
}
