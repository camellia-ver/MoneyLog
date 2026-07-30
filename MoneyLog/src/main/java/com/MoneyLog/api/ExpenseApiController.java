package com.MoneyLog.api;

import com.MoneyLog.dto.CategorySummaryDto;
import com.MoneyLog.dto.ExpenseDto;
import com.MoneyLog.dto.ExpenseSummaryResponseDto;
import com.MoneyLog.model.Expense;
import com.MoneyLog.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseApiController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto.Response> createExpense(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ExpenseDto.Request request
            ){
        Expense expense = expenseService.createExpense(userId, request);
        ExpenseDto.Response result = ExpenseDto.Response.from(expense);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
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
    public ResponseEntity<ExpenseDto.Response> updateExpense(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseDto.Request request
    ){
        Expense expense = expenseService.updateExpense(userId, expenseId, request);
        return ResponseEntity.ok(ExpenseDto.Response.from(expense));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto.Response>> getExpenses(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate
    ){
        List<ExpenseDto.Response> response = expenseService.searchExpense(userId, categoryId, startDate, endDate)
                .stream()
                .map(ExpenseDto.Response::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponseDto> getSummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        BigDecimal totalAmount = expenseService.getTotalAmount(userId, startDate, endDate);
        List<CategorySummaryDto> categorySummaries = expenseService.getCategorySummary(userId, startDate, endDate);

        ExpenseSummaryResponseDto response = new ExpenseSummaryResponseDto(totalAmount, categorySummaries);

        return  ResponseEntity.ok(response);
    }
}
