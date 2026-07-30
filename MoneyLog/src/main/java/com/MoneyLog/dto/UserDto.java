package com.MoneyLog.dto;

import com.MoneyLog.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class UserDto {

    @Data
    public static class SignUpRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "사용자명은 필수입니다.")
        private String userName;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        private String password;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String email;
        private String userName;

        public static Response from(User user) {
            return new Response(user.getId(), user.getEmail(), user.getUserName());
        }
    }

    @Data
    public static class UpdateUserNameRequest {
        @NotBlank(message = "사용자명은 필수입니다.")
        private String userName;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        private String newPassword;
    }

    @Data
    public static class DeleteAccountRequest {
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }
}