package com.MoneyLog.exception;

public class ExpenseNotFoundException extends RuntimeException{
    public ExpenseNotFoundException(){
        super("지출 내역을 찾을 수 없습니다.");
    }
}
