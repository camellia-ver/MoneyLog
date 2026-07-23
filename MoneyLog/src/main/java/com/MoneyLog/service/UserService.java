package com.MoneyLog.service;

import com.MoneyLog.dto.SignUpRequestDto;
import com.MoneyLog.enums.Role;
import com.MoneyLog.exception.DuplicateEmailException;
import com.MoneyLog.exception.InvalidCredentialsException;
import com.MoneyLog.exception.UserNotFoundException;
import com.MoneyLog.model.User;
import com.MoneyLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(SignUpRequestDto inputData){
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

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException();
        }

        return user;
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
}
