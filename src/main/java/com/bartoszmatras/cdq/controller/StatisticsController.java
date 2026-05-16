package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;
import com.bartoszmatras.cdq.dto.StatisticsResponse;
import com.bartoszmatras.cdq.service.StatisticsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Transaction statistics and aggregation")
public class StatisticsController {

    private final StatisticsServiceImpl statisticsServiceImpl;

    @GetMapping("/by-category")
    @Operation(summary = "Get transaction statistics grouped by category for a given month")
    public ResponseEntity<StatisticsResponse<CategoryStatistics>> byCategory(
            @Parameter(description = "Month in format yyyy-MM, e.g. 2026-01")
            @RequestParam("month") String month) {

        var data = statisticsServiceImpl.getStatisticsByCategory(month);
        return ResponseEntity.ok(StatisticsResponse.<CategoryStatistics>builder()
                .queryDescription("Statistics by category for " + month)
                .data(data)
                .build());
    }

    @GetMapping("/by-iban")
    @Operation(summary = "Get transaction statistics grouped by IBAN for a given month")
    public ResponseEntity<StatisticsResponse<IbanStatistics>> byIban(
            @Parameter(description = "Month in format yyyy-MM, e.g. 2026-01")
            @RequestParam("month") String month) {

        var data = statisticsServiceImpl.getStatisticsByIban(month);
        return ResponseEntity.ok(StatisticsResponse.<IbanStatistics>builder()
                .queryDescription("Statistics by IBAN for " + month)
                .data(data)
                .build());
    }

    @GetMapping("/by-month")
    @Operation(summary = "Get transaction statistics grouped by month for a date range")
    public ResponseEntity<StatisticsResponse<MonthlyStatistics>> byMonth(
            @Parameter(description = "Start month (yyyy-MM)") @RequestParam("from") String from,
            @Parameter(description = "End month (yyyy-MM)") @RequestParam("to") String to) {

        var data = statisticsServiceImpl.getStatisticsByMonth(from, to);
        return ResponseEntity.ok(StatisticsResponse.<MonthlyStatistics>builder()
                .queryDescription("Monthly statistics from " + from + " to " + to)
                .data(data)
                .build());
    }
}