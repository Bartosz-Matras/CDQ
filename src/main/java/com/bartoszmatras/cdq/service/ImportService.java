package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.model.ImportJob;

import java.util.List;

public interface ImportService {
    ImportJob createJob(String originalFilename);
    ImportJob getJob(String jobId);
    List<ImportJob> getAllJobs();
    void processFileAsync(String jobId, java.nio.file.Path filePath);
}
