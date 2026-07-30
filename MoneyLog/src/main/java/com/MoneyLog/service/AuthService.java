package com.MoneyLog.service;

import com.MoneyLog.dto.AuthDto;
import com.MoneyLog.model.User;
import com.MoneyLog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthDto.Response login(AuthDto.Request request){
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtTokenProvider.createToken(user);

        return new AuthDto.Response(token, user.getId(), jwtTokenProvider.getExpirationMillis());
    }
}
