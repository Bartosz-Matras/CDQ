package com.bartoszmatras.cdq.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
@CompoundIndex(
        name = "yearMonth_category",
        def = "{'yearMonth': 1, 'category': 1}"
)
@CompoundIndex(
        name = "yearMonth_iban",
        def = "{'yearMonth': 1, 'iban': 1}"
)
public class Transaction {

    @Id
    private String id;

    private String iban;
    private LocalDate date;
    private String currency;
    private TransactionCategory category;
    private BigDecimal amount;

    @Indexed
    private String yearMonth; // "2026-01"

    @Indexed
    private String importJobId;
}