package com.bartoszmatras.cdq.validation;

import com.bartoszmatras.cdq.dto.CsvTransactionRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RowValidatorTest {

    private RowValidator rowValidator;
    private CsvTransactionRow row;

    @BeforeEach
    void setUp() {
        rowValidator = new RowValidator();
        row = mock(CsvTransactionRow.class);
    }

    private void mockValidRow() {
        when(row.getIban()).thenReturn("DE89370400440532013000");
        when(row.getDate()).thenReturn("2023-01-15");
        when(row.getCurrency()).thenReturn("EUR");
        when(row.getCategory()).thenReturn("GROCERIES");
        when(row.getAmount()).thenReturn("100.50");
    }

    @Test
    void validate_shouldReturnOk_whenAllFieldsAreValid() {
        // given
        mockValidRow();

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, IBAN is required",
            "'', IBAN is required",
            "' ', IBAN is required",
            "DE123, Invalid IBAN",
            "PL, Invalid IBAN",
            "1234567890, Invalid IBAN"
    }, nullValues = {"null"})
    void validate_shouldReturnError_whenIbanIsInvalid(String invalidIban, String expectedError) {
        // given
        mockValidRow();
        when(row.getIban()).thenReturn(invalidIban);

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().getFirst()).startsWith(expectedError);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, Date is required",
            "'', Date is required",
            "' ', Date is required",
            "2099-01-01, Date cannot be in the future",
            "15-01-2023, Invalid date format",
            "2023/01/15, Invalid date format",
            "abc, Invalid date format"
    }, nullValues = {"null"})
    void validate_shouldReturnError_whenDateIsInvalid(String invalidDate, String expectedError) {
        // given
        mockValidRow();
        when(row.getDate()).thenReturn(invalidDate);

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().getFirst()).startsWith(expectedError);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, Currency is required",
            "'', Currency is required",
            "' ', Currency is required",
            "XYZ, Invalid currency code",
            "123, Invalid currency code",
            "EURO, Invalid currency code"
    }, nullValues = {"null"})
    void validate_shouldReturnError_whenCurrencyIsInvalid(String invalidCurrency, String expectedError) {
        // given
        mockValidRow();
        when(row.getCurrency()).thenReturn(invalidCurrency);

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().getFirst()).startsWith(expectedError);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, Category is required",
            "'', Category is required",
            "' ', Category is required",
            "INVALID_CAT, Invalid category",
            "SOME_UNKNOWN, Invalid category",
            "123, Invalid category"
    }, nullValues = {"null"})
    void validate_shouldReturnError_whenCategoryIsInvalid(String invalidCategory, String expectedError) {
        // given
        mockValidRow();
        when(row.getCategory()).thenReturn(invalidCategory);

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().getFirst()).startsWith(expectedError);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, Amount is required",
            "'', Amount is required",
            "' ', Amount is required",
            "100.123, Amount must have at most 2 decimal places",
            "0.001, Amount must have at most 2 decimal places",
            "abc, Invalid amount: not a valid number",
            "100.5.5, Invalid amount: not a valid number"
    }, nullValues = {"null"})
    void validate_shouldReturnError_whenAmountIsInvalid(String invalidAmount, String expectedError) {
        // given
        mockValidRow();
        when(row.getAmount()).thenReturn(invalidAmount);

        // when
        var result = rowValidator.validate(row);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().getFirst()).startsWith(expectedError);
    }

    @Test
    void validate_shouldReturnAggregatedErrors_whenMultipleFieldsAreInvalid() {
        // given
        when(row.getIban()).thenReturn("");
        when(row.getDate()).thenReturn("bad-date");
        when(row.getCurrency()).thenReturn("");
        when(row.getCategory()).thenReturn("");
        when(row.getAmount()).thenReturn("abc");

        // when
        var result = rowValidator.validate(row);

        //
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(5);
    }
}
