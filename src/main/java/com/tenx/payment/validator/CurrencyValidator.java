package com.tenx.payment.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<AllowedCurrencies, Currency> {

    private String[] allowedCurrencies;

    @Override
    public void initialize(AllowedCurrencies constraintAnnotation) {
        allowedCurrencies = constraintAnnotation.allowedCurrencies();
    }

    @Override
    public boolean isValid(Currency value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        for (String currency : allowedCurrencies) {
            if (currency.equalsIgnoreCase(value.getCurrencyCode())) {
                return true;
            }
        }

        return false;
    }
}
