package com.bartoszmatras.cdq.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "import_jobs")
public class ImportJob {

    @Id
    private String id;

    private String fileName;
    private ImportStatus status;

    private int totalRows;
    private int successRows;
    private int failedRows;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    private Instant createdAt;
    private Instant completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int line;
        private String reason;
    }
}