package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.model.ImportJob;
import com.bartoszmatras.cdq.model.ImportStatus;
import com.bartoszmatras.cdq.service.ImportServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    public static final String JOB_ID = "job123";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportServiceImpl importServiceImpl;

    @Test
    void uploadCsv_shouldFailWhenEmptyFile() throws Exception {
        // given
        var file = new MockMultipartFile("file", "test.csv",
                MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        // when & then
        mockMvc.perform(multipart("/api/v1/imports").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Uploaded file is empty"));
    }

    @Test
    void uploadCsv_shouldFailWhenNotCsvFile() throws Exception {
        // given
        var file = new MockMultipartFile("file", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/v1/imports").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Only CSV files are accepted"));
    }

    @Test
    void uploadCsv_shouldSuccess() throws Exception {
        // given
        var originalFilename = "test.csv";
        var file = new MockMultipartFile("file", originalFilename,
                MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        var job = ImportJob.builder()
                .id(JOB_ID)
                .status(ImportStatus.PROCESSING)
                .build();

        Mockito.when(importServiceImpl.createAndProcessJob(any(), anyString())).thenReturn(job);

        // when & then
        mockMvc.perform(multipart("/api/v1/imports").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(JOB_ID))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void getImportStatus_shouldFailWhenJobNotFound() throws Exception {
        // given
        Mockito.when(importServiceImpl.getJob(JOB_ID)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/imports/job123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImportStatus_shouldSuccessWhenJobFound() throws Exception {
        // given
        var job = ImportJob.builder()
                .id(JOB_ID)
                .status(ImportStatus.COMPLETED)
                .build();
        Mockito.when(importServiceImpl.getJob(JOB_ID)).thenReturn(job);

        // when & then
        mockMvc.perform(get("/api/v1/imports/job123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(JOB_ID))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void listImports_shouldReturnJobs() throws Exception {
        // given
        var job = ImportJob.builder()
                .id(JOB_ID)
                .status(ImportStatus.COMPLETED)
                .build();
        Mockito.when(importServiceImpl.getAllJobs()).thenReturn(List.of(job));

        // when & then
        mockMvc.perform(get("/api/v1/imports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value(JOB_ID));
    }
}
