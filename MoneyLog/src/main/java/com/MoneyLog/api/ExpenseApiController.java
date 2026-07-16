package com.MoneyLog.api;

import com.MoneyLog.dto.CategoryResponseDto;
import com.MoneyLog.dto.ExpenseRequestDto;
import com.MoneyLog.dto.ExpenseResponseDto;
import com.MoneyLog.model.Expense;
import com.MoneyLog.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getExpenses(@AuthenticationPrincipal Long userId){
        List<ExpenseResponseDto> result = expenseService.getExpenses(userId).stream()
                .map(ExpenseResponseDto::from)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long expenseId
    ){
        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequestDto request
    ){
        Expense expense = expenseService.updateExpense(userId, expenseId, request);
        return ResponseEntity.ok(ExpenseResponseDto.from(expense));
    }
}
