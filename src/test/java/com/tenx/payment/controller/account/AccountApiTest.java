package com.tenx.payment.controller.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.payment.controller.ApiErrorDetails;
import com.tenx.payment.dto.account.AccountRequestDto;
import com.tenx.payment.dto.account.AccountResponseDto;
import com.tenx.payment.model.Account;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAccountApi() throws Exception {
        // Given
        AccountRequestDto account = AccountRequestDto.builder()
                .balance(BigDecimal.TEN)
                .currency(Currency.getInstance("USD"))
                .build();
        String requestBody = objectMapper.writeValueAsString(account);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/account").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        // Then
        AccountResponseDto accountResponseDto =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AccountResponseDto.class);
        Account accountInDb = accountService.findAccountById(accountResponseDto.getId());

        assertThat(accountResponseDto.getId()).isEqualTo(accountInDb.getId());
        assertThat(accountResponseDto.getBalance().setScale(2, RoundingMode.HALF_UP)).isEqualTo(accountInDb.getBalance().setScale(2, RoundingMode.HALF_UP));
        assertThat(accountResponseDto.getCurrency()).isEqualTo(accountInDb.getCurrency());
        assertThat(accountResponseDto.getCreatedAtTimestamp()).isEqualTo(accountInDb.getCreateAtTimestamp());
    }

    @Test
    void createAccountNegativeInvalidBalance() throws Exception {
        // Given
        AccountRequestDto account = AccountRequestDto.builder()
                .balance(BigDecimal.valueOf(-100))
                .currency(Currency.getInstance("USD"))
                .build();
        String requestBody = objectMapper.writeValueAsString(account);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/account").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("balance field : must be greater than or equal to 0");
    }

    @Test
    void createAccountNoBalance() throws Exception {
        // Given
        AccountRequestDto account = AccountRequestDto.builder()
                .currency(Currency.getInstance("USD"))
                .build();
        String requestBody = objectMapper.writeValueAsString(account);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/account").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("balance field : must not be null");
    }

    @Test
    void createAccountNotSupportedCurrency() throws Exception {
        // Given
        AccountRequestDto account = AccountRequestDto.builder()
                .balance(BigDecimal.TEN)
                .currency(Currency.getInstance("RUB"))
                .build();
        String requestBody = objectMapper.writeValueAsString(account);

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/account").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).isEqualTo("currency field : Invalid currency supplied");
    }

    @Test
    void createAccountInvalidJson() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString("{}");

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/rest/account").content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isBadRequest()).andReturn();
        ApiErrorDetails apiErrorDetails =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiErrorDetails.class);

        // Then
        assertThat(apiErrorDetails.getMessages().get(0)).startsWith("JSON parse error");
    }

    @Test
    void getAccountApi() throws Exception {
        // Given
        Account createdAccount = accountService.saveAccount(new Account(BigDecimal.TEN, Currency.getInstance("USD")));
        MockHttpServletRequestBuilder requestBuilder = get("/api/rest/account/{id}", createdAccount.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        // Then
        AccountResponseDto accountResponseDto =  objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AccountResponseDto.class);
        assertThat(accountResponseDto.getId()).isEqualTo(createdAccount.getId());
        assertThat(accountResponseDto.getBalance().setScale(2, RoundingMode.HALF_UP)).isEqualTo(createdAccount.getBalance().setScale(2, RoundingMode.HALF_UP));
        assertThat(accountResponseDto.getCurrency()).isEqualTo(createdAccount.getCurrency());
        assertThat(accountResponseDto.getCreatedAtTimestamp()).isEqualTo(createdAccount.getCreateAtTimestamp());
    }

    @Test
    void getAccountApiNonExistingAccount() throws Exception {
        // Given
        long id = 7;
        MockHttpServletRequestBuilder requestBuilder = get("/api/rest/account/{id}", id);

        // When
        // Then
        mockMvc.perform(requestBuilder).andExpect(status().isNotFound()).andReturn();
    }
}