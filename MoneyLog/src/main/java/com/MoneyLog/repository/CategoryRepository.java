package com.MoneyLog.repository;

import com.MoneyLog.model.Category;
import com.MoneyLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByUserAndName(User user, String name);
    List<Category> findByUser(User user);
}
