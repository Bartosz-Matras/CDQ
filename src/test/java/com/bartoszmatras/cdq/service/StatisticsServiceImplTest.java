package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Mock
    private AggregationResults<CategoryStatistics> categoryAggregationResults;

    @Mock
    private AggregationResults<IbanStatistics> ibanAggregationResults;

    @Mock
    private AggregationResults<MonthlyStatistics> monthlyAggregationResults;

    @Test
    void shouldGetStatisticsByCategory() {
        // given
        var yearMonth = "2023-10";
        var stat = new CategoryStatistics("GROCERIES", BigDecimal.valueOf(100.00), 2L);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(CategoryStatistics.class)))
                .thenReturn(categoryAggregationResults);
        when(categoryAggregationResults.getMappedResults()).thenReturn(List.of(stat));

        // when
        var result = statisticsService.getStatisticsByCategory(yearMonth);

        // then
        assertEquals(1, result.size());
        assertEquals("GROCERIES", result.getFirst().getCategory());
        assertEquals(BigDecimal.valueOf(100.00), result.getFirst().getTotalAmount());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("transactions"), eq(CategoryStatistics.class));
    }

    @Test
    void shouldGetStatisticsByIban() {
        // given
        var yearMonth = "2023-10";
        var stat = new IbanStatistics("PL12345", BigDecimal.valueOf(500.00), BigDecimal.valueOf(-100.00),
                BigDecimal.valueOf(400.00), 2L);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(IbanStatistics.class)))
                .thenReturn(ibanAggregationResults);
        when(ibanAggregationResults.getMappedResults()).thenReturn(List.of(stat));

        // when
        var result = statisticsService.getStatisticsByIban(yearMonth);

        // then
        assertEquals(1, result.size());
        assertEquals("PL12345", result.getFirst().getIban());
        assertEquals(BigDecimal.valueOf(500.00), result.getFirst().getTotalIncome());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("transactions"), eq(IbanStatistics.class));
    }

    @Test
    void shouldGetStatisticsByMonth() {
        // given
        var fromMonth = "2023-01";
        var toMonth = "2023-10";
        var stat = new MonthlyStatistics("2023-05", BigDecimal.valueOf(1000.00), BigDecimal.valueOf(-500.00),
                BigDecimal.valueOf(500.00), 10L);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(MonthlyStatistics.class)))
                .thenReturn(monthlyAggregationResults);
        when(monthlyAggregationResults.getMappedResults()).thenReturn(List.of(stat));

        // when
        var result = statisticsService.getStatisticsByMonth(fromMonth, toMonth);

        // then
        assertEquals(1, result.size());
        assertEquals("2023-05", result.getFirst().getMonth());
        assertEquals(BigDecimal.valueOf(500.00), result.getFirst().getNetBalance());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("transactions"), eq(MonthlyStatistics.class));
    }
}
