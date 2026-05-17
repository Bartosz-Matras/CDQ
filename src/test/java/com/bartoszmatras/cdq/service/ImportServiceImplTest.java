package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.repository.ImportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceImplTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private ImportAsyncService importAsyncService;

    @InjectMocks
    private ImportServiceImpl importService;

    private ImportJob testJob;

    private static final String JOB_ID = "job-123";

    @BeforeEach
    void setUp() {
        testJob = ImportJob.builder()
                .id(JOB_ID)
                .fileName("test.csv")
                .status(ImportStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldCreateAndProcessJob() throws IOException {
        // given
        when(importJobRepository.save(any(ImportJob.class))).thenReturn(testJob);

        var file = new MockMultipartFile("file", "test.csv", "text/csv", "dummy".getBytes());

        // when
        var result = importService.createAndProcessJob(file, "test.csv");

        // then
        assertNotNull(result);
        assertEquals(JOB_ID, result.getId());
        assertEquals(ImportStatus.PENDING, result.getStatus());

        verify(importJobRepository).save(any(ImportJob.class));
        verify(importAsyncService).processFileAsync(eq(JOB_ID), any(Path.class));
    }

    @Test
    void shouldGetJobWhenExists() {
        // given
        when(importJobRepository.findById(JOB_ID)).thenReturn(Optional.of(testJob));

        // when
        var result = importService.getJob(JOB_ID);

        // then
        assertNotNull(result);
        assertEquals(JOB_ID, result.getId());
        verify(importJobRepository).findById(JOB_ID);
    }

    @Test
    void shouldReturnNullWhenJobDoesNotExist() {
        // given
        when(importJobRepository.findById(JOB_ID)).thenReturn(Optional.empty());

        // when
        var result = importService.getJob(JOB_ID);

        // then
        assertNull(result);
        verify(importJobRepository).findById(JOB_ID);
    }

    @Test
    void shouldGetAllJobs() {
        // given
        when(importJobRepository.findAll()).thenReturn(List.of(testJob));

        // when
        var result = importService.getAllJobs();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(JOB_ID, result.getFirst().getId());
        verify(importJobRepository).findAll();
    }

    @Test
    void shouldCreateJob() {
        // given
        when(importJobRepository.save(any(ImportJob.class))).thenReturn(testJob);

        // when
        var result = importService.createJob("test.csv");

        // then
        assertNotNull(result);
        assertEquals(JOB_ID, result.getId());
        assertEquals("test.csv", result.getFileName());

        verify(importJobRepository).save(any(ImportJob.class));
    }
}
