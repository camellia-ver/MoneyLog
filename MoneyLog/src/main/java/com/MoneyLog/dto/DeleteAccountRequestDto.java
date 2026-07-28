package com.MoneyLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDto {
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
