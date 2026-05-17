package com.bartoszmatras.cdq.service;

public interface ImportAsyncService {
    void processFileAsync(String jobId, java.nio.file.Path filePath);
}
