package com.bartoszmatras.cdq.validation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RowValidationResultTest {

    @Test
    void ok_shouldCreateValidResultWithNoErrors() {
        var result = RowValidationResult.ok();

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void fail_shouldCreateInvalidResultWithErrors() {
        var errors = List.of("Error 1", "Error 2");
        var result = RowValidationResult.fail(errors);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactlyElementsOf(errors);
    }
}
