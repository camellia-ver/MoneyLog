package com.MoneyLog.service;

import com.MoneyLog.dto.ExpenseRequestDto;
import com.MoneyLog.exception.ExpenseAccessDeniedException;
import com.MoneyLog.exception.ExpenseNotFoundException;
import com.MoneyLog.model.Category;
import com.MoneyLog.model.Expense;
import com.MoneyLog.model.User;
import com.MoneyLog.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense createExpense(Long userId, ExpenseRequestDto request){
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

    public List<Expense> getExpenses(Long userId){
        User user = userService.getUserById(userId);
        return expenseRepository.findByUser(user);
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId){
        Expense expense = getExpenseByIdAndUser(expenseId, userId);
        expenseRepository.delete(expense);
    }

    @Transactional
    public Expense updateExpense(Long userId, Long expenseId, ExpenseRequestDto request){
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
}
