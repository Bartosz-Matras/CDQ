package com.bartoszmatras.cdq.validation;

import com.bartoszmatras.cdq.dto.CsvTransactionRow;
import com.bartoszmatras.cdq.model.TransactionCategory;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RowValidator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> VALID_CURRENCIES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toSet());

    public RowValidationResult validate(CsvTransactionRow row) {
        List<String> errors = new ArrayList<>();

        validateIban(row.getIban(), errors);
        validateDate(row.getDate(), errors);
        validateCurrency(row.getCurrency(), errors);
        validateCategory(row.getCategory(), errors);
        validateAmount(row.getAmount(), errors);

        return errors.isEmpty() ? RowValidationResult.ok() : RowValidationResult.fail(errors);
    }

    private void validateIban(String iban, List<String> errors) {
        if (iban == null || iban.isBlank()) {
            errors.add("IBAN is required");
            return;
        }
        try {
            IbanUtil.validate(iban.trim());
        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
            errors.add("Invalid IBAN: " + e.getMessage());
        }
    }

    private void validateDate(String date, List<String> errors) {
        if (date == null || date.isBlank()) {
            errors.add("Date is required");
            return;
        }
        try {
            var parsed = LocalDate.parse(date.trim(), DATE_FORMAT);
            if (parsed.isAfter(LocalDate.now())) {
                errors.add("Date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            errors.add("Invalid date format, expected yyyy-MM-dd");
        }
    }

    private void validateCurrency(String currency, List<String> errors) {
        if (currency == null || currency.isBlank()) {
            errors.add("Currency is required");
            return;
        }
        if (!VALID_CURRENCIES.contains(currency.trim().toUpperCase())) {
            errors.add("Invalid currency code: " + currency);
        }
    }

    private void validateCategory(String category, List<String> errors) {
        if (category == null || category.isBlank()) {
            errors.add("Category is required");
            return;
        }
        if (!TransactionCategory.isValid(category)) {
            errors.add("Invalid category: " + category + ". Valid values: " +
                    Arrays.toString(TransactionCategory.values()));
        }
    }

    private void validateAmount(String amount, List<String> errors) {
        if (amount == null || amount.isBlank()) {
            errors.add("Amount is required");
            return;
        }
        try {
            var value = new BigDecimal(amount.trim());
            if (value.scale() > 2) {
                errors.add("Amount must have at most 2 decimal places");
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid amount: not a valid number");
        }
    }
}
