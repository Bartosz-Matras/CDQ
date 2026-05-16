package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;

import java.util.List;

public interface StatisticsService {
    List<CategoryStatistics> getStatisticsByCategory(String yearMonth);
    List<IbanStatistics> getStatisticsByIban(String yearMonth);
    List<MonthlyStatistics> getStatisticsByMonth(String fromMonth, String toMonth);
}
