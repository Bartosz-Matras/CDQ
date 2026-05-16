package com.bartoszmatras.cdq.dto;


import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ImportStatusResponse {
    private String jobId;
    private String fileName;
    private ImportStatus status;
    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<ImportJob.RowError> errors;
    private Instant createdAt;
    private Instant completedAt;

    public static ImportStatusResponse from(ImportJob job) {
        return ImportStatusResponse.builder()
                .jobId(job.getId())
                .fileName(job.getFileName())
                .status(job.getStatus())
                .totalRows(job.getTotalRows())
                .successRows(job.getSuccessRows())
                .failedRows(job.getFailedRows())
                .errors(job.getErrors())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
