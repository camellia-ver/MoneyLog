package com.MoneyLog.api;

import com.MoneyLog.dto.AuthDto;
import com.MoneyLog.service.AuthService;
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
    public AuthDto.Response login(@RequestBody AuthDto.Request request){
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        // TODO: 지금 당장은 특별한 로직 없음 차후 기능 확장 가능
        return ResponseEntity.ok().build();
    }
}
