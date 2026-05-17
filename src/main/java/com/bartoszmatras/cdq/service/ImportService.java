package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.model.ImportJob;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImportService {
    ImportJob createAndProcessJob(MultipartFile file, String originalFilename) throws IOException;
    ImportJob createJob(String originalFilename);
    ImportJob getJob(String jobId);
    List<ImportJob> getAllJobs();
}
