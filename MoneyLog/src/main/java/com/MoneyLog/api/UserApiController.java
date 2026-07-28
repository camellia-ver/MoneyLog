package com.MoneyLog.api;

import com.MoneyLog.dto.*;
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
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request){
        User user = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponseDto.from(user));
    }

    @PutMapping("/me/username")
    public ResponseEntity<UserResponseDto> updateUserName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateUserNameRequestDto request
            ){
        User user = userService.updateUserName(userId, request.getUserName());
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequestDto request
            ){
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DeleteAccountRequestDto request
            ){
        userService.deleteAccount(userId, request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
