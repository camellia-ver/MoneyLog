package com.MoneyLog.api;

import com.MoneyLog.dto.SignUpResponseDto;
import com.MoneyLog.dto.SignUpRequestDto;
import com.MoneyLog.dto.UpdateUserNameRequestDto;
import com.MoneyLog.model.User;
import com.MoneyLog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @PostMapping("signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request){
        User user = userService.signUp(request);

        SignUpResponseDto dto = new SignUpResponseDto(user.getId(), user.getEmail(), user.getUserName());

        ResponseEntity<SignUpResponseDto> result = ResponseEntity.status(HttpStatus.CREATED)
                .body(dto);


        return result;
    }

    @PutMapping("/me/username")
    public ResponseEntity<SignUpResponseDto> updateUserName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateUserNameRequestDto request
            ){
        User user = userService.updateUserName(userId, request.getUserName());
        return ResponseEntity.ok(new SignUpResponseDto(user.getId(), user.getEmail(), user.getUserName()));
    }
}
