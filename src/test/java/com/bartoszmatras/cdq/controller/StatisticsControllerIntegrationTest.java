package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.AbstractIntegrationTest;
import com.bartoszmatras.cdq.model.Transaction;
import com.bartoszmatras.cdq.model.TransactionCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StatisticsControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.save(Transaction.builder()
                .iban("DE1234567890")
                .date(LocalDate.of(2026, 1, 15))
                .currency("EUR")
                .category(TransactionCategory.GROCERIES)
                .amount(BigDecimal.valueOf(-50.0))
                .yearMonth("2026-01")
                .build(), "transactions");

        mongoTemplate.save(Transaction.builder()
                .iban("DE1234567890")
                .date(LocalDate.of(2026, 1, 20))
                .currency("EUR")
                .category(TransactionCategory.SALARY)
                .amount(BigDecimal.valueOf(2000.0))
                .yearMonth("2026-01")
                .build(), "transactions");

        mongoTemplate.save(Transaction.builder()
                .iban("PL0987654321")
                .date(LocalDate.of(2026, 2, 10))
                .currency("PLN")
                .category(TransactionCategory.ENTERTAINMENT)
                .amount(BigDecimal.valueOf(-100.0))
                .yearMonth("2026-02")
                .build(), "transactions");
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("transactions");
    }

    @Test
    void shouldReturnStatisticsByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/by-category")
                        .param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$.data[1].category").value("SALARY"));
    }

    @Test
    void shouldReturnStatisticsByIban() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/by-iban")
                        .param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].iban").value("DE1234567890"))
                .andExpect(jsonPath("$.data[0].transactionCount").value(2))
                .andExpect(jsonPath("$.data[0].balance").value(1950.0));
    }

    @Test
    void shouldReturnStatisticsByMonth() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/by-month")
                        .param("from", "2026-01")
                        .param("to", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value("2026-01"))
                .andExpect(jsonPath("$.data[0].transactionCount").value(2))
                .andExpect(jsonPath("$.data[1].month").value("2026-02"))
                .andExpect(jsonPath("$.data[1].transactionCount").value(1));
    }
}
