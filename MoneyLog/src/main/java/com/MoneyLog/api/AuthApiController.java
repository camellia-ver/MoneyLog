package com.MoneyLog.api;

import com.MoneyLog.dto.LoginRequestDto;
import com.MoneyLog.dto.LoginResponseDto;
import com.MoneyLog.model.User;
import com.MoneyLog.security.JwtTokenProvider;
import com.MoneyLog.service.AuthService;
import com.MoneyLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthService authService;

    @PostMapping("/api/users/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request){
        return authService.login(request);
    }
}
