package com.bartoszmatras.cdq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatistics {
    private String category;
    private BigDecimal totalAmount;
    private long count;
}
