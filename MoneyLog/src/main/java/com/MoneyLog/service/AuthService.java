package com.MoneyLog.service;

import com.MoneyLog.dto.LoginRequestDto;
import com.MoneyLog.dto.LoginResponseDto;
import com.MoneyLog.model.User;
import com.MoneyLog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDto login(LoginRequestDto request){
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtTokenProvider.createToken(user);

        return new LoginResponseDto(token, user.getId(), jwtTokenProvider.getExpirationMillis());
    }
}
