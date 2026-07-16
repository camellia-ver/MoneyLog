package com.MoneyLog.exception;

public class CategoryAccessDeniedException extends RuntimeException{
    public CategoryAccessDeniedException(){
        super("해당 카테고리에 접근할 권한이 없습니다.");
    }
}
