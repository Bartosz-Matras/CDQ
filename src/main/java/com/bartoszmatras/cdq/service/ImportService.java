package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.model.ImportJob;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ImportService {
    ImportJob createAndProcessJob(MultipartFile file, String originalFilename) throws IOException;
    Optional<ImportJob> getJob(String jobId);
    List<ImportJob> getAllJobs();
}
