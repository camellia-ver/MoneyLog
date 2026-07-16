package com.MoneyLog.api;

import com.MoneyLog.dto.CategoryRequestDto;
import com.MoneyLog.dto.CategoryResponseDto;
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
    public ResponseEntity<CategoryResponseDto> createCategory(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CategoryRequestDto request
            ){
        Category category = categoryService.createCategory(userId, request.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryResponseDto.from(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @AuthenticationPrincipal Long userId
    ){
        List<CategoryResponseDto> response = categoryService.getCategories(userId).stream()
                .map(CategoryResponseDto::from)
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
}
