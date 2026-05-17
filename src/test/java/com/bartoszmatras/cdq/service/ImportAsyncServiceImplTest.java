package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CsvTransactionRow;
import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.model.Transaction;
import com.bartoszmatras.cdq.repository.ImportJobRepository;
import com.bartoszmatras.cdq.validation.RowValidationResult;
import com.bartoszmatras.cdq.validation.RowValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportAsyncServiceImplTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private RowValidator rowValidator;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CsvParserService.CsvParser csvParser;

    @InjectMocks
    private ImportAsyncServiceImpl importAsyncService;

    @TempDir
    Path tempDir;

    private Path csvFile;

    private static final String JOB_ID = "job-123";

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(importAsyncService, "chunkSize", 100);
        csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "dummy,content");
    }

    @Test
    void shouldProcessFileSuccessfully() throws Exception {
        // given
        var job = ImportJob.builder().id(JOB_ID).status(ImportStatus.PROCESSING).build();
        when(importJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(csvParserService.parse(any(Reader.class))).thenReturn(csvParser);

        var validRow = CsvTransactionRow.builder()
                .lineNumber(1)
                .iban("PL12345678901234567890123456")
                .date("2023-10-10")
                .currency("PLN")
                .category("GROCERIES")
                .amount("100.50")
                .build();

        var invalidRow = CsvTransactionRow.builder()
                .lineNumber(2)
                .iban("INVALID")
                .date("2023-10-10")
                .currency("PLN")
                .category("GROCERIES")
                .amount("100.50")
                .build();

        when(csvParser.nextChunk(100))
                .thenReturn(List.of(validRow, invalidRow))
                .thenReturn(Collections.emptyList());

        when(rowValidator.validate(validRow)).thenReturn(RowValidationResult.ok());
        when(rowValidator.validate(invalidRow)).thenReturn(RowValidationResult.fail(List.of("Invalid IBAN")));

        // when
        importAsyncService.processFileAsync(JOB_ID, csvFile);

        // then
        assertEquals(ImportStatus.COMPLETED, job.getStatus());
        assertEquals(2, job.getTotalRows());
        assertEquals(1, job.getSuccessRows());
        assertEquals(1, job.getFailedRows());
        assertNotNull(job.getCompletedAt());
        assertEquals(1, job.getErrors().size());
        assertEquals("Invalid IBAN", job.getErrors().getFirst().getReason());

        verify(importJobRepository, times(2)).save(job);
        verify(mongoTemplate, times(1)).insertAll(anyCollection());
    }

    @Test
    void shouldHandleExceptionAndRollback() throws Exception {
        // given
        var job = ImportJob.builder().id(JOB_ID).status(ImportStatus.PROCESSING).build();
        when(importJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));

        // Simulating error during parsing
        when(csvParserService.parse(any(Reader.class))).thenThrow(new RuntimeException("Error reading file"));

        // when
        importAsyncService.processFileAsync(JOB_ID, csvFile);

        // then
        assertEquals(ImportStatus.FAILED, job.getStatus());
        assertEquals(0, job.getSuccessRows());
        assertEquals(1, job.getErrors().size());
        assertTrue(job.getErrors().getFirst().getReason().contains("Error reading file"));

        verify(importJobRepository, times(2)).save(job);
        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(Transaction.class));
    }
}
