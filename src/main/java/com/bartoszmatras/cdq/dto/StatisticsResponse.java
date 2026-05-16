package com.bartoszmatras.cdq.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsResponse<T> {
    private String queryDescription;
    private List<T> data;
}