package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CategoryStatistics;
import com.bartoszmatras.cdq.dto.IbanStatistics;
import com.bartoszmatras.cdq.dto.MonthlyStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService{

    private static final String ID = "_id";
    private static final String YEAR_MONTH = "yearMonth";
    private static final String CATEGORY = "category";
    private static final String IBAN = "iban";
    private static final String AMOUNT = "amount";
    private static final String TOTAL_AMOUNT = "totalAmount";
    private static final String COUNT = "count";
    private static final String TRANSACTIONS = "transactions";
    private static final String TOTAL_INCOME = "totalIncome";
    private static final String TOTAL_EXPENSE = "totalExpense";
    private static final String BALANCE = "balance";
    private static final String TRANSACTION_COUNT = "transactionCount";
    private static final String NET_BALANCE = "netBalance";
    private static final String MONTH = "month";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<CategoryStatistics> getStatisticsByCategory(String yearMonth) {
        var aggregation = newAggregation(
                match(Criteria.where(YEAR_MONTH).is(yearMonth)),
                group(CATEGORY)
                        .sum(AMOUNT).as(TOTAL_AMOUNT)
                        .count().as(COUNT),
                project()
                        .and(ID).as(CATEGORY)
                        .andInclude(TOTAL_AMOUNT, COUNT),
                sort(Sort.Direction.ASC, CATEGORY)
        );

        return mongoTemplate
                .aggregate(aggregation, TRANSACTIONS, CategoryStatistics.class)
                .getMappedResults();
    }

    @Override
    public List<IbanStatistics> getStatisticsByIban(String yearMonth) {
        var incomeExpr = Cond.when(Criteria.where(AMOUNT).gt(0))
                .thenValueOf(AMOUNT).otherwise(0);

        var expenseExpr = Cond.when(Criteria.where(AMOUNT).lt(0))
                .thenValueOf(AMOUNT).otherwise(0);

        var aggregation = newAggregation(
                match(Criteria.where(YEAR_MONTH).is(yearMonth)),
                group(IBAN)
                        .sum(incomeExpr).as(TOTAL_INCOME)
                        .sum(expenseExpr).as(TOTAL_EXPENSE)
                        .sum(AMOUNT).as(BALANCE)
                        .count().as(TRANSACTION_COUNT),
                project()
                        .and(ID).as(IBAN)
                        .andInclude(TOTAL_INCOME, TOTAL_EXPENSE, BALANCE, TRANSACTION_COUNT),
                sort(Sort.Direction.ASC, IBAN)
        );

        return mongoTemplate
                .aggregate(aggregation, TRANSACTIONS, IbanStatistics.class)
                .getMappedResults();
    }

    @Override
    public List<MonthlyStatistics> getStatisticsByMonth(String fromMonth, String toMonth) {
        var incomeExpr = Cond.when(Criteria.where(AMOUNT).gt(0))
                .thenValueOf(AMOUNT).otherwise(0);

        var expenseExpr = Cond.when(Criteria.where(AMOUNT).lt(0))
                .thenValueOf(AMOUNT).otherwise(0);

        var aggregation = newAggregation(
                match(Criteria.where(YEAR_MONTH).gte(fromMonth).lte(toMonth)),
                group(YEAR_MONTH)
                        .sum(incomeExpr).as(TOTAL_INCOME)
                        .sum(expenseExpr).as(TOTAL_EXPENSE)
                        .sum(AMOUNT).as(NET_BALANCE)
                        .count().as(TRANSACTION_COUNT),
                project()
                        .and(ID).as(MONTH)
                        .andInclude(TOTAL_INCOME, TOTAL_EXPENSE, NET_BALANCE, TRANSACTION_COUNT),
                sort(Sort.Direction.ASC, MONTH)
        );

        return mongoTemplate
                .aggregate(aggregation, TRANSACTIONS, MonthlyStatistics.class)
                .getMappedResults();
    }
}