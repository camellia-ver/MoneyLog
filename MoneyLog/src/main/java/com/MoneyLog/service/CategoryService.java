package com.MoneyLog.service;

import com.MoneyLog.exception.DuplicateCategoryException;
import com.MoneyLog.model.Category;
import com.MoneyLog.model.User;
import com.MoneyLog.repository.CategoryRepository;
import com.MoneyLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional
    public Category createCategory(Long userId, String name){
        User user = userService.getUserById(userId);

        if (categoryRepository.existsByUserAndName(user, name)){
            throw new DuplicateCategoryException();
        }

        Category category = Category.builder()
                .user(user)
                .name(name)
                .build();

        return categoryRepository.save(category);
    }

    public List<Category> getCategories(Long userId){
        User user = userService.getUserById(userId);
        return categoryRepository.findByUser(user);
    }
}
