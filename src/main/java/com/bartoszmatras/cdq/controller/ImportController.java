package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.dto.ImportResponse;
import com.bartoszmatras.cdq.dto.ImportStatusResponse;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.service.ImportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
@Tag(name = "Import", description = "CSV file import operations")
public class ImportController {

    private final ImportServiceImpl importServiceImpl;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a CSV file with bank transactions")
    public ResponseEntity<ImportResponse> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ImportResponse.builder()
                            .status(ImportStatus.FAILED)
                            .message("Uploaded file is empty")
                            .build()
            );
        }

        // TODO Make it simpler
        var originalFilename = file.getOriginalFilename();
        if (isNotCsv(originalFilename)) {
            return ResponseEntity.badRequest().body(
                    ImportResponse.builder()
                            .status(ImportStatus.FAILED)
                            .message("Only CSV files are accepted")
                            .build()
            );
        }

        var job = importServiceImpl.createJob(originalFilename);

        try {
            var tempFile = Files.createTempFile("import-" + job.getId() + "-", ".csv");
            file.transferTo(tempFile.toFile());
            importServiceImpl.processFileAsync(job.getId(), tempFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportResponse.builder()
                            .status(ImportStatus.FAILED)
                            .message("Failed to initialize import")
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ImportResponse.builder()
                        .jobId(job.getId())
                        .status(ImportStatus.PROCESSING)
                        .message("File accepted. Use GET /api/imports/" + job.getId() + " to check status.")
                        .build()
        );
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Check the status of an import job")
    public ResponseEntity<ImportStatusResponse> getImportStatus(@PathVariable("jobId") String jobId) {
        var job = importServiceImpl.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ImportStatusResponse.from(job));
    }

    @GetMapping
    @Operation(summary = "List all import jobs")
    public ResponseEntity<List<ImportStatusResponse>> listImports() {
        var jobs = importServiceImpl.getAllJobs().stream()
                .map(ImportStatusResponse::from)
                .toList();
        return ResponseEntity.ok(jobs);
    }

    private boolean isNotCsv(String filename) {
        return filename == null || !filename.toLowerCase().endsWith(".csv");
    }
}