package com.MoneyLog.dto;

import com.MoneyLog.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExpenseResponseDto {
    private Long id;
    private String categoryName;
    private BigDecimal amount;
    private String content;
    private String memo;
    private LocalDateTime createdAt;

    public static ExpenseResponseDto from(Expense expense){
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getCategory().getName(),
                expense.getAmount(),
                expense.getContent(),
                expense.getMemo(),
                expense.getCreatedAt()
        );
    }
}
