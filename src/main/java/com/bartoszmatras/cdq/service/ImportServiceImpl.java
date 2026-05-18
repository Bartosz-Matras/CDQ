package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ImportJobRepository importJobRepository;
    private final ImportAsyncService importAsyncService;

    @Override
    public ImportJob createAndProcessJob(MultipartFile file, String originalFilename) throws IOException {
        var job = createJob(originalFilename);
        var tempFile = Files.createTempFile("import-" + job.getId() + "-", ".csv");
        file.transferTo(tempFile.toFile());
        log.debug("Saved uploaded file to temporary location: {}", tempFile);
        importAsyncService.processFileAsync(job.getId(), tempFile);
        return job;
    }

    @Override
    public Optional<ImportJob> getJob(String jobId) {
        return importJobRepository.findById(jobId);
    }

    @Override
    public List<ImportJob> getAllJobs() {
        return importJobRepository.findAll();
    }

    @Override
    public ImportJob createJob(String fileName) {
        var job = ImportJob.builder()
                .fileName(fileName)
                .status(ImportStatus.PENDING)
                .createdAt(Instant.now())
                .errors(new ArrayList<>())
                .build();
        var savedJob = importJobRepository.save(job);
        log.debug("Created import job with ID: {}", savedJob.getId());
        return savedJob;
    }
}