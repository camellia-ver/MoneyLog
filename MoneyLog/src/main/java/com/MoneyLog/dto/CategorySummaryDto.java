package com.MoneyLog.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategorySummaryDto {
    private String categoryName;
    private BigDecimal totalAmount;

    public CategorySummaryDto(String categoryName, BigDecimal totalAmount){
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }
}
