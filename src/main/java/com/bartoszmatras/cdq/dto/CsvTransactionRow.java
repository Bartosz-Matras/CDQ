package com.bartoszmatras.cdq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvTransactionRow {
    private int lineNumber;
    private String iban;
    private String date;
    private String currency;
    private String category;
    private String amount;
}