package com.tenx.payment.controller.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.payment.controller.ApiErrorDetails;
import com.tenx.payment.dto.transaction.TransactionRequestDto;
import com.tenx.payment.dto.transaction.TransactionResponseDto;
import com.tenx.payment.model.Account;
import com.tenx.payment.model.Transaction;
import com.tenx.payment.repository.TransactionRepository;
import com.tenx.payment.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void executeInvalidTransactionEmptyBody() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString("{}");

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).startsWith("JSON parse error");
    }

    @Test
    void executeInvalidTransactionUnsupportedCurrency() throws Exception {
        // Given
        TransactionRequestDto transactionRequestDto = mockTransactionRequestDto("RUB", 50);
        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("currency field : Invalid currency supplied");
    }

    @Test
    void executeInvalidTransactionNegativeAmount() throws Exception {
        // Given
        TransactionRequestDto transactionRequestDto = mockTransactionRequestDto("USD", -50);
        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("amount field : must be greater than 0");
    }

    @Test
    void executeInvalidTransactionSameAccount() throws Exception {
        // Given
        TransactionRequestDto transactionRequestDto = mockTransactionRequestDto("USD", 50);
        transactionRequestDto.setTargetAccountId(transactionRequestDto.getSourceAccountId());
        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("Source and target account must be different");
    }

    @Test
    void executeInvalidTransactionNotEnoughAmount() throws Exception {
        // Given
        TransactionRequestDto transactionRequestDto = mockTransactionRequestDto("USD", 50);
        transactionRequestDto.setAmount(BigDecimal.valueOf(10000));
        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("Insufficient amount");
    }

    @Test
    void executeTransaction() throws Exception {
        // Given
        TransactionRequestDto transactionRequestDto = mockTransactionRequestDto("USD", 50);
        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/transaction").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        // Then
        TransactionResponseDto transactionResponseDto =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionResponseDto.class);
        Transaction transaction = transactionRepository.findById(transactionResponseDto.getId()).get();

        assertThat(transactionResponseDto.getId()).isEqualTo(transaction.getId());
        assertThat(transactionResponseDto.getAmount().setScale(2, RoundingMode.HALF_UP)).isEqualTo(transaction.getAmount().setScale(2, RoundingMode.HALF_UP));
        assertThat(transactionResponseDto.getCurrency()).isEqualTo(transaction.getCurrency());
        assertThat(transactionResponseDto.getSourceAccountId()).isEqualTo(transaction.getSourceAccount().getId());
        assertThat(transactionResponseDto.getTargetAccountId()).isEqualTo(transaction.getTargetAccount().getId());
    }


    private TransactionRequestDto mockTransactionRequestDto(String USD, int amount) {
        Currency usdCurrency = Currency.getInstance(USD);
        Account sourceAccount = accountService.saveAccount(new Account(BigDecimal.valueOf(100), usdCurrency));
        Account targetAccount = accountService.saveAccount(new Account(BigDecimal.ZERO, usdCurrency));

        return new TransactionRequestDto(BigDecimal.valueOf(amount), sourceAccount.getId(), targetAccount.getId(), usdCurrency);
    }
}