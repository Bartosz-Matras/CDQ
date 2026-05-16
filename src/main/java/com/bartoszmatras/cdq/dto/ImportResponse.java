package com.bartoszmatras.cdq.dto;

import com.bartoszmatras.cdq.model.ImportStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResponse {
    private String jobId;
    private ImportStatus status;
    private String message;
}