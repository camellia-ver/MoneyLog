package com.MoneyLog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExpenseSummaryResponseDto {
    private BigDecimal totalAmount;
    private List<CategorySummaryDto> categorySummaryList;
}
