package com.bartoszmatras.cdq.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleMaxUploadSize() {
        var ex = new MaxUploadSizeExceededException(5000L);
        var response = handler.handleMaxUploadSize(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "File too large");
        assertThat(response.getBody()).containsKey(GlobalExceptionHandler.TIMESTAMP);
    }

    @Test
    void shouldHandleIllegalArgument() {
        var ex = new IllegalArgumentException("Invalid arg");
        var response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .containsEntry(GlobalExceptionHandler.ERROR, GlobalExceptionHandler.BAD_REQUEST)
                .containsEntry(GlobalExceptionHandler.MESSAGE, "Invalid arg");
    }

    @Test
    void shouldHandleHttpMediaTypeNotSupported() {
        var ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, List.of(MediaType.MULTIPART_FORM_DATA));
        var response = handler.handleHttpMediaTypeNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Unsupported Media Type");
    }

    @Test
    void shouldHandleMultipartException() {
        var ex = new MultipartException("Multipart error");
        var response = handler.handleMultipartException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, GlobalExceptionHandler.BAD_REQUEST);
    }

    @Test
    void shouldHandleMissingServletRequestParameter() {
        var ex = new MissingServletRequestParameterException("file", "MultipartFile");
        var response = handler.handleMissingServletRequestParameter(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Bad request");
        assertThat(response.getBody().get(GlobalExceptionHandler.MESSAGE).toString()).contains("file");
    }

    @Test
    void shouldHandleGeneral() {
        var ex = new Exception("General error");
        var response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry(GlobalExceptionHandler.ERROR, "Internal server error");
    }
}
