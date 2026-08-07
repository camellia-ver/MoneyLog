package com.MoneyLog.repository;

import com.MoneyLog.dto.CategorySummaryDto;
import com.MoneyLog.model.Category;
import com.MoneyLog.model.Expense;
import com.MoneyLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user = :user " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR e.createdAt <= :endDate)")
    List<Expense> searchExpenses(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new com.MoneyLog.dto.CategorySummaryDto(e.category.name, SUM(e.amount)) " +
        "FROM Expense e " +
        "WHERE e.user = :user AND e.createdAt BETWEEN :start AND :end " +
        "GROUP BY e.category.name")
    List<CategorySummaryDto> getCategorySummary(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(e.amount) FROM Expense e " +
            "WHERE e.user = :user AND e.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalAmount(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByCategory(Category category);
    void deleteAllByUser(User user);
}
