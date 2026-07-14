package com.MoneyLog.repository;

import com.MoneyLog.model.Category;
import com.MoneyLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByUserAndName(User user, String name);
}
