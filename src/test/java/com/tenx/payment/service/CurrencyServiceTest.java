package com.tenx.payment.service;

import com.tenx.payment.exception.UnsupportedCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CurrencyServiceTest {

    private CurrencyService currencyService;

    @BeforeEach
    public void setUp() {
        currencyService = new CurrencyService();
    }

    @Test
    void convertSameCurrency() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        Currency currency = Currency.getInstance("USD");

        // When
        BigDecimal convertedAmount = currencyService.convertCurrency(amount, currency, currency);

        // Then
        assertThat(convertedAmount).isEqualTo(amount);

    }

    @Test
    void convertCurrencyFromUSDToEUR() {
        // Given
        BigDecimal amount = new BigDecimal("54.95");
        Currency fromCurrency = Currency.getInstance("USD");
        Currency toCurrency = Currency.getInstance("EUR");

        // When
        BigDecimal convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency);

        // Then
        BigDecimal expectedAmount = new BigDecimal("50.00");
        assertThat(convertedAmount).isEqualTo(expectedAmount);
    }

    @Test
    void convertCurrencyFromEURToUSD() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");
        Currency fromCurrency = Currency.getInstance("EUR");
        Currency toCurrency = Currency.getInstance("USD");

        // When
        BigDecimal convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency);

        // Then
        BigDecimal expectedAmount = new BigDecimal("54.95");
        assertThat(convertedAmount).isEqualTo(expectedAmount);
    }

    @Test
    void convertCurrencyFromGBPToBGN() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");
        Currency fromCurrency = Currency.getInstance("GBP");
        Currency toCurrency = Currency.getInstance("BGN");

        // When
        BigDecimal convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency);

        // Then
        BigDecimal expectedAmount = new BigDecimal("114.10");
        assertThat(convertedAmount).isEqualTo(expectedAmount);
    }

    @Test
    void convertCurrencyFromBGNToGBP() {
        BigDecimal amount = new BigDecimal("114.10");
        Currency fromCurrency = Currency.getInstance("BGN");
        Currency toCurrency = Currency.getInstance("GBP");

        BigDecimal convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency);

        BigDecimal expectedAmount = new BigDecimal("50.00");
        assertThat(convertedAmount).isEqualTo(expectedAmount);
    }

    @Test
    void convertToUnsupportedCurrencyRub() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        Currency fromCurrency = Currency.getInstance("USD");
        Currency unsupportedCurrency = Currency.getInstance("RUB");

        // When
        // Then
        assertThatThrownBy(() -> currencyService.convertCurrency(amount, fromCurrency, unsupportedCurrency))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessage("Not supported currency format, only USD, EUR, GBP and BGN supported");
    }
}