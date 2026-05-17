package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CsvTransactionRow;
import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.model.Transaction;
import com.bartoszmatras.cdq.model.TransactionCategory;
import com.bartoszmatras.cdq.repository.ImportJobRepository;
import com.bartoszmatras.cdq.validation.RowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportAsyncServiceImpl implements ImportAsyncService{

    private final ImportJobRepository importJobRepository;
    private final CsvParserService csvParserService;
    private final RowValidator rowValidator;
    private final MongoTemplate mongoTemplate;

    @Value("${import.chunk-size:1000}")
    private int chunkSize;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Async("importTaskExecutor")
    public void processFileAsync(String jobId, Path csvFilePath) {
        var job = importJobRepository.findById(jobId).orElseThrow();
        job.setStatus(ImportStatus.PROCESSING);
        importJobRepository.save(job);

        int totalRows = 0;
        int successRows = 0;
        int failedRows = 0;
        List<ImportJob.RowError> errors = new ArrayList<>();

        try (var reader = Files.newBufferedReader(csvFilePath, StandardCharsets.UTF_8);
             var parser = csvParserService.parse(reader)) {

            List<CsvTransactionRow> chunk;
            do {
                chunk = parser.nextChunk(chunkSize);
                if (chunk.isEmpty()) break;

                var validTransactions = new ArrayList<>();

                for (var row : chunk) {
                    totalRows++;
                    var result = rowValidator.validate(row);

                    if (result.isValid()) {
                        validTransactions.add(mapToTransaction(row, jobId));
                        successRows++;
                    } else {
                        failedRows++;
                        errors.add(ImportJob.RowError.builder()
                                .line(row.getLineNumber())
                                .reason(String.join("; ", result.getErrors()))
                                .build());
                    }
                }

                if (!validTransactions.isEmpty()) {
                    var saved = mongoTemplate.insertAll(validTransactions);
                    log.debug("Inserted {} transactions for job {}", saved.size(), jobId);
                }

            } while (!chunk.isEmpty());

            job.setStatus(ImportStatus.COMPLETED);
            log.info("Import job {} completed: {}/{} rows successful", jobId, successRows, totalRows);

        } catch (Exception e) {
            log.error("Import job {} failed. Rolling back saved transactions.", jobId, e);
            job.setStatus(ImportStatus.FAILED);
            errors.add(ImportJob.RowError.builder()
                    .line(0)
                    .reason("Fatal error: " + e.getMessage())
                    .build());

            // Rollback already inserted transactions for this job to maintain consistency
            var query = Query.query(Criteria.where("importJobId").is(jobId));
            mongoTemplate.remove(query, Transaction.class);

            successRows = 0;
        }

        job.setTotalRows(totalRows);
        job.setSuccessRows(successRows);
        job.setFailedRows(failedRows);
        job.setErrors(errors);
        job.setCompletedAt(Instant.now());
        importJobRepository.save(job);
    }

    private Transaction mapToTransaction(CsvTransactionRow row, String jobId) {
        var date = LocalDate.parse(row.getDate().trim(), DATE_FORMAT);
        var yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

        return Transaction.builder()
                .iban(row.getIban().trim().toUpperCase())
                .date(date)
                .currency(row.getCurrency().trim().toUpperCase())
                .category(TransactionCategory.valueOf(row.getCategory().trim().toUpperCase()))
                .amount(new BigDecimal(row.getAmount().trim()))
                .yearMonth(yearMonth)
                .importJobId(jobId)
                .build();
    }
}
