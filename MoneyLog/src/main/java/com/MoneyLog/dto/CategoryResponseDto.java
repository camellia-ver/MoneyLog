package com.MoneyLog.dto;

import com.MoneyLog.model.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;

    public static CategoryResponseDto from(Category category){
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}
