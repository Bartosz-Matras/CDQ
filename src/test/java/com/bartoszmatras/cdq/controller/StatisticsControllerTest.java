package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;
import com.bartoszmatras.cdq.service.StatisticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsServiceImpl statisticsServiceImpl;

    @Test
    void byCategory_shouldReturnData() throws Exception {
        // given
        var category = "GROCERIES";
        var totalAmount = BigDecimal.valueOf(100.23);
        var count = 4;
        var stats = new CategoryStatistics(category, totalAmount, count);
        var categoryStatisticsList = List.of(stats);
        Mockito.when(statisticsServiceImpl.getStatisticsByCategory("2026-01")).thenReturn(categoryStatisticsList);

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-category").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Statistics by category for 2026-01"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(categoryStatisticsList.size()))
                .andExpect(jsonPath("$.data[0].category").value(category))
                .andExpect(jsonPath("$.data[0].totalAmount").value(totalAmount))
                .andExpect(jsonPath("$.data[0].count").value(count));
    }

    @Test
    void byCategory_shouldReturnNoData() throws Exception {
        // given
        Mockito.when(statisticsServiceImpl.getStatisticsByCategory("2026-01")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-category").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Statistics by category for 2026-01"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void byIban_shouldReturnData() throws Exception {
        // given
        var iban = "DE12345678901234567890";
        var totalIncome = BigDecimal.valueOf(5200.55);
        var totalExpense = BigDecimal.valueOf(-2200.05);
        var balance = BigDecimal.valueOf(3000.50);
        var transactionCount = 12;
        var stats = new IbanStatistics(iban, totalIncome, totalExpense, balance, transactionCount);
        var ibanStatisticsList = List.of(stats);
        Mockito.when(statisticsServiceImpl.getStatisticsByIban("2026-01")).thenReturn(ibanStatisticsList);

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-iban").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Statistics by IBAN for 2026-01"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(ibanStatisticsList.size()))
                .andExpect(jsonPath("$.data[0].balance").value(balance))
                .andExpect(jsonPath("$.data[0].iban").value(iban))
                .andExpect(jsonPath("$.data[0].totalExpense").value(totalExpense))
                .andExpect(jsonPath("$.data[0].totalIncome").value(totalIncome))
                .andExpect(jsonPath("$.data[0].transactionCount").value(transactionCount));
    }

    @Test
    void byIban_shouldReturnNoData() throws Exception {
        // given
        Mockito.when(statisticsServiceImpl.getStatisticsByIban("2026-01")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-iban").param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Statistics by IBAN for 2026-01"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void byMonth_shouldReturnData() throws Exception {
        // given
        var month = "2026-01";
        var totalIncome = BigDecimal.valueOf(5200.55);
        var totalExpense = BigDecimal.valueOf(-2200.05);
        var netBalance = BigDecimal.valueOf(3000.50);
        var transactionCount = 2;
        var stats = new MonthlyStatistics(month,totalIncome, totalExpense, netBalance, transactionCount);
        var monthlyStatisticsList = List.of(stats);
        Mockito.when(statisticsServiceImpl.getStatisticsByMonth("2026-01", "2026-02")).thenReturn(monthlyStatisticsList);

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-month").param("from", "2026-01").param("to", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Monthly statistics from 2026-01 to 2026-02"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(monthlyStatisticsList.size()))
                .andExpect(jsonPath("$.data[0].month").value(month))
                .andExpect(jsonPath("$.data[0].netBalance").value(netBalance))
                .andExpect(jsonPath("$.data[0].totalExpense").value(totalExpense))
                .andExpect(jsonPath("$.data[0].totalIncome").value(totalIncome))
                .andExpect(jsonPath("$.data[0].transactionCount").value(transactionCount));
    }

    @Test
    void byMonth_shouldReturnNoData() throws Exception {
        // given
        Mockito.when(statisticsServiceImpl.getStatisticsByMonth("2026-01", "2026-02")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/statistics/by-month").param("from", "2026-01").param("to", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryDescription").value("Monthly statistics from 2026-01 to 2026-02"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
