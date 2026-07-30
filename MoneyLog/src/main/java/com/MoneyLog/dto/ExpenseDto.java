package com.MoneyLog.dto;

import com.MoneyLog.model.Expense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseDto {
    @Data
    public static class Request {
        @NotNull(message = "카테고리는 필수입니다.")
        private Long categoryId;

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        private BigDecimal amount;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String memo;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String categoryName;
        private BigDecimal amount;
        private String content;
        private String memo;
        private LocalDateTime createdAt;

        public static ExpenseDto.Response from(Expense expense){
            return new ExpenseDto.Response(
                    expense.getId(),
                    expense.getCategory().getName(),
                    expense.getAmount(),
                    expense.getContent(),
                    expense.getMemo(),
                    expense.getCreatedAt()
            );
        }
    }
}
