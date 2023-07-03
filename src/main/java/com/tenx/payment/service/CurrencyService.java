package com.tenx.payment.service;

import com.tenx.payment.exception.UnsupportedCurrencyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {

    private final Map<Currency, BigDecimal> exchangeRates;

    public CurrencyService() {
        // 1 USD used as a base currency for intermediate conversions aka
        // GBP -> EUR
        // First we convert the GBP to USD
        // and then USD to BGN
        exchangeRates = new HashMap<>();
        exchangeRates.put(Currency.getInstance("USD"), new BigDecimal("1.00"));
        exchangeRates.put(Currency.getInstance("EUR"), new BigDecimal("0.91"));
        exchangeRates.put(Currency.getInstance("GBP"), new BigDecimal("0.78"));
        exchangeRates.put(Currency.getInstance("BGN"), new BigDecimal("1.78"));
    }

    /**
     * Converts amount from a given currency to targeted currency e.g. 50 USD to 46 EUR.
     * It uses USD basis for internal conversions for mock purposes, as we don't have real conversion rates.
     *
     * For the purposes of the assessment only USD, EUR, GBP and BGN are supported.
     *
     * @param amount to be converted
     * @param fromCurrency from source currency
     * @param toCurrency to targeted currency
     * @return the converted amount in the targeted currency
     */
    public BigDecimal convertCurrency(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal fromRate = exchangeRates.get(fromCurrency);
        BigDecimal toRate = exchangeRates.get(toCurrency);

        if (fromRate == null || toRate == null) {
            throw new UnsupportedCurrencyException("Not supported currency format, only USD, EUR, GBP and BGN supported");
        }

        // Convert to USD as the intermediary currency
        BigDecimal amountInUSD = amount.divide(fromRate, 2, RoundingMode.HALF_UP);

        // Convert to the target currency
        return amountInUSD.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }
}
