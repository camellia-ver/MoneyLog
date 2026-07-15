package com.MoneyLog.api;

import com.MoneyLog.dto.SignUpResponseDto;
import com.MoneyLog.dto.UserDto;
import com.MoneyLog.model.User;
import com.MoneyLog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @PostMapping("signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody UserDto request){
        User user = userService.signUp(request);

        SignUpResponseDto dto = new SignUpResponseDto(user.getId(), user.getEmail(), user.getUserName());

        ResponseEntity<SignUpResponseDto> result = ResponseEntity.status(HttpStatus.CREATED)
                .body(dto);


        return result;
    }
}
