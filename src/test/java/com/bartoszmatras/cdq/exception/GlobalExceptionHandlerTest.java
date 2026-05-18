package com.bartoszmatras.cdq.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleMaxUploadSize() {
        // given
        var ex = new MaxUploadSizeExceededException(5000L);
        // when
        var response = handler.handleMaxUploadSize(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "File too large");
        assertThat(response.getBody()).containsKey(GlobalExceptionHandler.TIMESTAMP);
    }

    @Test
    void shouldHandleIllegalArgument() {
        // given
        var ex = new IllegalArgumentException("Invalid arg");
        // when
        var response = handler.handleIllegalArgument(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .containsEntry(GlobalExceptionHandler.ERROR, GlobalExceptionHandler.BAD_REQUEST)
                .containsEntry(GlobalExceptionHandler.MESSAGE, "Invalid arg");
    }

    @Test
    void shouldHandleHttpMediaTypeNotSupported() {
        // given
        var ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON,
                List.of(MediaType.MULTIPART_FORM_DATA));
        // when
        var response = handler.handleHttpMediaTypeNotSupported(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Unsupported Media Type");
    }

    @Test
    void shouldHandleMultipartException() {
        // given
        var ex = new MultipartException("Multipart error");
        // when
        var response = handler.handleMultipartException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, GlobalExceptionHandler.BAD_REQUEST);
    }

    @Test
    void shouldHandleMissingServletRequestParameter() {
        // given
        var ex = new MissingServletRequestParameterException("file", "MultipartFile");
        // when
        var response = handler.handleMissingServletRequestParameter(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Bad request");
        assertThat(response.getBody().get(GlobalExceptionHandler.MESSAGE).toString()).contains("file");
    }

    @Test
    void shouldHandleRejectedExecution() {
        // given
        var ex = new RejectedExecutionException("Import service is at capacity. Please try again later.");
        // when
        var response = handler.handleRejectedExecution(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .containsEntry(GlobalExceptionHandler.ERROR, "Service unavailable")
                .containsEntry(GlobalExceptionHandler.MESSAGE, "Import service is at capacity. Please try again later.");
        assertThat(response.getBody()).containsKey(GlobalExceptionHandler.TIMESTAMP);
    }

    @Test
    void shouldHandleGeneral() {
        // given
        var ex = new Exception("General error");
        // when
        var response = handler.handleGeneral(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Internal server error");
    }
}
