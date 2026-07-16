package com.MoneyLog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequestDto {
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotNull(message = "금액은 필수입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private String memo;
}
