package com.MoneyLog.api;

import com.MoneyLog.dto.LoginRequestDto;
import com.MoneyLog.dto.LoginResponseDto;
import com.MoneyLog.model.User;
import com.MoneyLog.security.JwtTokenProvider;
import com.MoneyLog.service.AuthService;
import com.MoneyLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthApiController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request){
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        // TODO: 지금 당장은 특별한 로직 없음 차후 기능 확장 가능
        return ResponseEntity.ok().build();
    }
}
