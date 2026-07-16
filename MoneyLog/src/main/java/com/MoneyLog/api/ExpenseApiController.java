package com.MoneyLog.api;

import com.MoneyLog.dto.ExpenseRequestDto;
import com.MoneyLog.dto.ExpenseResponseDto;
import com.MoneyLog.model.Expense;
import com.MoneyLog.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseApiController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ExpenseRequestDto request
            ){
        Expense expense = expenseService.createExpense(userId, request);
        ExpenseResponseDto result = ExpenseResponseDto.from(expense);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
