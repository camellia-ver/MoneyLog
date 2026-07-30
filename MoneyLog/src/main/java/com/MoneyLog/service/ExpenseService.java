package com.MoneyLog.service;

import com.MoneyLog.dto.CategorySummaryDto;
import com.MoneyLog.dto.ExpenseDto;
import com.MoneyLog.exception.ExpenseAccessDeniedException;
import com.MoneyLog.exception.ExpenseNotFoundException;
import com.MoneyLog.model.Category;
import com.MoneyLog.model.Expense;
import com.MoneyLog.model.User;
import com.MoneyLog.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense createExpense(Long userId, ExpenseDto.Request request){
        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryByIdAndUser(request.getCategoryId(), userId);

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .content(request.getContent())
                .memo(request.getMemo())
                .build();

        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId){
        Expense expense = getExpenseByIdAndUser(expenseId, userId);
        expenseRepository.delete(expense);
    }

    @Transactional
    public Expense updateExpense(Long userId, Long expenseId, ExpenseDto.Request request){
        Expense expense = getExpenseByIdAndUser(expenseId, userId);
        Category category = categoryService.getCategoryByIdAndUser(request.getCategoryId(), userId);

        expense.updateExpense(category, request.getAmount(), request.getContent(), request.getMemo());

        return expense;
    }

    public Expense getExpenseByIdAndUser(Long expenseId, Long userId){
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(ExpenseNotFoundException::new);

        if (!expense.getUser().getId().equals(userId)){
            throw new ExpenseAccessDeniedException();
        }

        return expense;
    }

    public List<Expense> searchExpense(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate){
        User user = userService.getUserById(userId);

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        return expenseRepository.searchExpenses(user, categoryId ,start, end);
    }

    public List<CategorySummaryDto> getCategorySummary(Long userId, LocalDate startDate, LocalDate endDate){
        User user = userService.getUserById(userId);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return expenseRepository.getCategorySummary(user,start,end);
    }

    public BigDecimal getTotalAmount(Long userId, LocalDate startDate, LocalDate endDate){
        User user = userService.getUserById(userId);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        BigDecimal total = expenseRepository.getTotalAmount(user, start, end);
        return  total != null ? total : BigDecimal.ZERO;
    }
}
