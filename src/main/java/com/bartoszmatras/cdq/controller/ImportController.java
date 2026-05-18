package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.dto.ImportResponse;
import com.bartoszmatras.cdq.dto.ImportStatusResponse;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
@Tag(name = "Import", description = "CSV file import operations")
public class ImportController {

    private final ImportService importService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a CSV file with bank transactions")
    public ResponseEntity<ImportResponse> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(getFailedResponse("Uploaded file is empty"));
        }

        var originalFilename = file.getOriginalFilename();
        if (isNotCsv(originalFilename)) {
            return ResponseEntity.badRequest()
                    .body(getFailedResponse("Only CSV files are accepted"));
        }

        try {
            var job = importService.createAndProcessJob(file, originalFilename);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    ImportResponse.builder()
                            .jobId(job.getId())
                            .status(ImportStatus.PROCESSING)
                            .message("File accepted. Use GET /api/v1/imports/" + job.getId() + " to check status.")
                            .build()
            );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(getFailedResponse("Failed to initialize import"));
        }
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Check the status of an import job")
    public ResponseEntity<ImportStatusResponse> getImportStatus(@PathVariable("jobId") String jobId) {
        return importService.getJob(jobId)
                .map(job -> ResponseEntity.ok(ImportStatusResponse.from(job)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all import jobs")
    public ResponseEntity<List<ImportStatusResponse>> listImports() {
        var jobs = importService.getAllJobs().stream()
                .map(ImportStatusResponse::from)
                .toList();
        return ResponseEntity.ok(jobs);
    }

    private boolean isNotCsv(String filename) {
        return filename == null || !filename.toLowerCase().endsWith(".csv");
    }

    private static ImportResponse getFailedResponse(String message) {
        return ImportResponse.builder()
                .status(ImportStatus.FAILED)
                .message(message)
                .build();
    }
}