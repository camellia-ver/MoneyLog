package com.MoneyLog.service;

import com.MoneyLog.dto.ExpenseRequestDto;
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
}
