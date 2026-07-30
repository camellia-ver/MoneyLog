package com.MoneyLog.api;

import com.MoneyLog.dto.UserDto;
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
    public ResponseEntity<UserDto.Response> signUp(@Valid @RequestBody UserDto.SignUpRequest request){
        User user = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserDto.Response.from(user));
    }

    @PutMapping("/me/username")
    public ResponseEntity<UserDto.Response> updateUserName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserDto.UpdateUserNameRequest request
            ){
        User user = userService.updateUserName(userId, request.getUserName());
        return ResponseEntity.ok(UserDto.Response.from(user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserDto.ChangePasswordRequest request
            ){
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserDto.DeleteAccountRequest request
            ){
        userService.deleteAccount(userId, request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
