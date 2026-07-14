package com.MoneyLog.service;

import com.MoneyLog.dto.UserDto;
import com.MoneyLog.exception.DuplicateEmailException;
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
    public User signUp(UserDto inputData){
        if (userRepository.existsByEmail(inputData.getEmail())){
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + inputData.getEmail());
        }

        User user = User.builder()
                .email(inputData.getEmail())
                .userName(inputData.getUserName())
                .password(passwordEncoder.encode(inputData.getPassword()))
                .build();

        return userRepository.save(user);
    }
}
