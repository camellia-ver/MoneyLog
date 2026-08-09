package com.MoneyLog.service;

import com.MoneyLog.dto.UserDto;
import com.MoneyLog.enums.Role;
import com.MoneyLog.exception.*;
import com.MoneyLog.model.User;
import com.MoneyLog.repository.CategoryRepository;
import com.MoneyLog.repository.ExpenseRepository;
import com.MoneyLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public User signUp(UserDto.SignUpRequest inputData){
        if (userRepository.existsByEmail(inputData.getEmail())){
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + inputData.getEmail());
        }

        User user = User.builder()
                .email(inputData.getEmail())
                .userName(inputData.getUserName())
                .password(passwordEncoder.encode(inputData.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User login(String email, String password) {
        if (loginAttemptService.isLocked(email)){
            throw new AccountLockedException();
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty() ||
                !passwordEncoder.matches(password, userOptional.get().getPassword())){
            loginAttemptService.loginFailed(email);
            throw new InvalidCredentialsException();
        }

        loginAttemptService.loginSucceeded(email);

        return userOptional.get();
    }

    @Transactional
    public User updateUserName(Long userId, String userName){
        User user = getUserById(userId);

        user.updateUserName(userName);

        return user;
    }

    public User getUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword){
        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new InvalidPasswordException();
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteAccount(Long userId, String password){
        User user = getUserById(userId);

        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidPasswordException();
        }

        expenseRepository.deleteAllByUser(user);
        categoryRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }
}
