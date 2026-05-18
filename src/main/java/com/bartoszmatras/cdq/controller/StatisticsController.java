package com.bartoszmatras.cdq.controller;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;
import com.bartoszmatras.cdq.dto.StatisticsResponse;
import com.bartoszmatras.cdq.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Transaction statistics and aggregation")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/by-category")
    @Operation(summary = "Get transaction statistics grouped by category for a given month")
    public ResponseEntity<StatisticsResponse<CategoryStatistics>> byCategory(
            @Parameter(description = "Month in format yyyy-MM, e.g. 2026-01")
            @RequestParam("month") String month) {

        validateMonth(month);
        var data = statisticsService.getStatisticsByCategory(month);
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

        validateMonth(month);
        var data = statisticsService.getStatisticsByIban(month);
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

        validateMonth(from);
        validateMonth(to);
        if (from.compareTo(to) > 0) {
            throw new IllegalArgumentException("'from' month must not be after 'to' month");
        }

        var data = statisticsService.getStatisticsByMonth(from, to);
        return ResponseEntity.ok(StatisticsResponse.<MonthlyStatistics>builder()
                .queryDescription("Monthly statistics from " + from + " to " + to)
                .data(data)
                .build());
    }

    private void validateMonth(String month) {
        try {
            YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid month format: '" + month + "'. Expected format: yyyy-MM (e.g. 2026-01)");
        }
    }
}