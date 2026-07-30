package com.MoneyLog.api;

import com.MoneyLog.dto.CategoryDto;
import com.MoneyLog.model.Category;
import com.MoneyLog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApiController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto.Response> createCategory(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CategoryDto.Request request
            ){
        Category category = categoryService.createCategory(userId, request.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryDto.Response.from(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto.Response>> getCategories(
            @AuthenticationPrincipal Long userId
    ){
        List<CategoryDto.Response> response = categoryService.getCategories(userId).stream()
                .map(CategoryDto.Response::from)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId
    ){
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{categoryId}")
    public  ResponseEntity<CategoryDto.Response> updateCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryDto.Request request
    ){
        Category category = categoryService.updateCategory(userId, categoryId, request.getName());
        return ResponseEntity.ok(CategoryDto.Response.from(category));
    }
}
