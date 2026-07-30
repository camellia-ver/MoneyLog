package com.MoneyLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class AuthDto {
    @Data
    public static class Request {
        private String email;
        private String password;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private String token;
        private Long userId;
        private long expiresIn;
    }
}
