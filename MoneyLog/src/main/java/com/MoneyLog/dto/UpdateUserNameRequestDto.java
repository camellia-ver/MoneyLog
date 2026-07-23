package com.MoneyLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserNameRequestDto {
    @NotBlank(message = "사용자명은 필수입니다.")
    private String userName;
}
