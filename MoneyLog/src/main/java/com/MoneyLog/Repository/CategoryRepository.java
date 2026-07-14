package com.MoneyLog.Repository;

import com.MoneyLog.Model.Category;
import com.MoneyLog.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByUserAndName(User user, String name);
}
