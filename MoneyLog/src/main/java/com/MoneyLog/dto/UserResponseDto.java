package com.MoneyLog.dto;

import com.MoneyLog.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String userName;

    public static UserResponseDto from(User user){
        return new UserResponseDto(user.getId(), user.getEmail(), user.getUserName());
    }
}
