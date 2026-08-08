package com.MoneyLog.exception;

public class AccountLockedException extends RuntimeException{
    public AccountLockedException(){
        super("로그인 시도 횟수를 초과했습니다. 15분 후 다시 시도해주세요.");
    }
}
