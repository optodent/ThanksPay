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
