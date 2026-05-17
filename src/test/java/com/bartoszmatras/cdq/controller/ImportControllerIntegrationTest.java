package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.AbstractIntegrationTest;
import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImportControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.save(ImportJob.builder()
                .id("job1")
                .fileName("test1.csv")
                .status(ImportStatus.COMPLETED)
                .totalRows(10)
                .successRows(10)
                .failedRows(0)
                .createdAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .errors(new ArrayList<>())
                .build(), "import_jobs");

        mongoTemplate.save(ImportJob.builder()
                .id("job2")
                .fileName("test2.csv")
                .status(ImportStatus.FAILED)
                .totalRows(7)
                .successRows(5)
                .failedRows(2)
                .createdAt(Instant.parse("2026-05-15T10:00:00Z"))
                .errors(new ArrayList<>())
                .build(), "import_jobs");
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("import_jobs");
        mongoTemplate.dropCollection("transactions");
    }

    @Test
    void shouldReturnAllJobs() throws Exception {
        mockMvc.perform(get("/api/v1/imports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].jobId").value("job1"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].jobId").value("job2"))
                .andExpect(jsonPath("$[1].status").value("FAILED"));
    }

    @Test
    void shouldReturnJobById() throws Exception {
        mockMvc.perform(get("/api/v1/imports/job1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job1"))
                .andExpect(jsonPath("$.fileName").value("test1.csv"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.successRows").value(10));
    }

    @Test
    void shouldReturnNotFoundWhenJobDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/imports/non_existing_job"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStartCsvImport() throws Exception {
        var fileContent = "iban,date,currency,category,amount\nDE1234567890,2026-01-01,EUR,GROCERIES,10.0";
        var mockFile = new MockMultipartFile("file", "test.csv", MediaType.TEXT_PLAIN_VALUE, fileContent.getBytes());

        mockMvc.perform(multipart("/api/v1/imports")
                        .file(mockFile))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
}
