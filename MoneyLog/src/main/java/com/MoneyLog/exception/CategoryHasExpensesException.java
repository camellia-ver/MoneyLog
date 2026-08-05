package com.MoneyLog.exception;

public class CategoryHasExpensesException extends RuntimeException{
    public CategoryHasExpensesException(){
        super("이 카테고리에 등록된 지출이 있어 삭제할 수 없습니다. 먼저 지출을 삭제하거나 다른 카테고리로 옮겨주세요.");
    }
}
