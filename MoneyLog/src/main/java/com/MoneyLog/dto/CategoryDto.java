package com.MoneyLog.dto;

import com.MoneyLog.model.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class CategoryDto {
    @Data
    public static class Request{
        @NotBlank(message = "카테고리명은 필수입니다.")
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;

        public static Response from(Category category) {
            return new Response(category.getId(), category.getName());
        }
    }
}
