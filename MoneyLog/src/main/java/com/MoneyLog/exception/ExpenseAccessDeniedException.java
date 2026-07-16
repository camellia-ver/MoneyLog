package com.MoneyLog.exception;

public class ExpenseAccessDeniedException extends RuntimeException{
    public ExpenseAccessDeniedException(){
        super("해당 지출 내역에 접근할 권한이 없습니다.");
    }
}
