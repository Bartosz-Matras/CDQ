package com.bartoszmatras.cdq.validation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RowValidationResult {
    private boolean valid;
    private List<String> errors;

    public static RowValidationResult ok() {
        return RowValidationResult.builder().valid(true).errors(List.of()).build();
    }

    public static RowValidationResult fail(List<String> errors) {
        return RowValidationResult.builder().valid(false).errors(errors).build();
    }
}